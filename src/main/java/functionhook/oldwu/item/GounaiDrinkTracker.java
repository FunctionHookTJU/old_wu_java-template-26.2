package functionhook.oldwu.item;

import java.util.Set;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;

import functionhook.oldwu.Old_Wu_java;

/**
 * 野生狗奶的隐藏饮用计数器。
 *
 * <p>每喝一次狗奶计数 +1（持久化于玩家实体附件中）：
 * <ul>
 *   <li>第 1 次：仅正常获得永恒效果。</li>
 *   <li>第 2 次：屏幕中央弹出字幕"你将迈入永恒"，并将游戏刻速度调整为 100。</li>
 *   <li>第 3 次：屏幕中央弹出字幕"『Made in Heaven』"，游戏刻速度调整为 9999 并永久保持；
 *       此后第 4 次饮用需等待 {@link #FOURTH_DRINK_COOLDOWN_SECONDS} 秒现实世界时间
 *       （不随加速后的游戏刻走）。</li>
 *   <li>第 4 次：屏幕中央弹出字幕"『宇宙热寂』"，游戏刻速度还原为 20，
 *       将玩家传送到虚空维度 heat_death，并把重生点设置在该维度
 *       （玩家死亡后只能在该维度重生，无法回到主世界）。热寂重生点像重生锚一样
 *       独立于主世界——设置前会暂存玩家原有的（主世界）重生点，不会将其覆写丢失。</li>
 *   <li>第 5 次及以后：仅递增计数器，不再触发任何新效果。</li>
 * </ul>
 *
 * <p>食用春秋肠会清零计数器并还原被暂存的主世界重生点；若玩家当前位于 heat_death 维度，
 * 还会将其传送回主世界。
 */
public final class GounaiDrinkTracker {
	/** 饮用次数附件（持久化，跨服务器重启保留）。 */
	private static final AttachmentType<Integer> DRINK_COUNT =
		AttachmentRegistry.createPersistent(Old_Wu_java.id("gounai_drink_count"), Codec.INT);

	/** 是否被热寂维度锁定重生点（持久化）。 */
	private static final AttachmentType<Boolean> TRAPPED =
		AttachmentRegistry.createPersistent(Old_Wu_java.id("gounai_trapped"), Codec.BOOL);

	/** 被热寂维度锁定前暂存的主世界重生点（持久化；原重生点为空时不存在该附件）。 */
	private static final AttachmentType<ServerPlayer.RespawnConfig> SAVED_RESPAWN =
		AttachmentRegistry.createPersistent(Old_Wu_java.id("gounai_saved_respawn"), ServerPlayer.RespawnConfig.CODEC);

	/** 第 3 次饮用后、第 4 次饮用前的现实时间冷却结束时刻（毫秒时间戳，0 = 无冷却）。 */
	private static final AttachmentType<Long> FOURTH_DRINK_COOLDOWN_END =
		AttachmentRegistry.createPersistent(Old_Wu_java.id("gounai_fourth_drink_cooldown"), Codec.LONG);

	/** 第 3 次饮用后需等待的现实秒数，之后才允许第 4 次饮用。 */
	private static final long FOURTH_DRINK_COOLDOWN_SECONDS = 20L;

	/** 宇宙热寂（heat_death）虚空维度。 */
	private static final ResourceKey<Level> HEAT_DEATH = ResourceKey.create(Registries.DIMENSION, Old_Wu_java.id("heat_death"));

	/** 热寂维度内的重生点（基岩上方）。 */
	private static final BlockPos HEAT_DEATH_RESPAWN_POS = new BlockPos(0, 1, 0);

	private GounaiDrinkTracker() {
	}

	/**
	 * 服务端调用：饮用野生狗奶后推进隐藏计数器并触发对应表现。
	 */
	public static void onDrink(ServerPlayer player) {
		int count = player.getAttachedOrElse(DRINK_COUNT, 0) + 1;
		player.setAttached(DRINK_COUNT, count);

		MinecraftServer server = ((ServerLevel) player.level()).getServer();
		if (count == 2) {
			showCenteredSubtitle(player, "你将迈入永恒");
			server.tickRateManager().setTickRate(100.0F);
		} else if (count == 3) {
			showCenteredSubtitle(player, "『Made in Heaven』");
			server.tickRateManager().setTickRate(9999.0F);
			// 第 4 次饮用的冷却：按现实世界时间（不随加速后的游戏刻走）
			player.setAttached(FOURTH_DRINK_COOLDOWN_END, Util.getMillis() + FOURTH_DRINK_COOLDOWN_SECONDS * 1000L);
		} else if (count == 4) {
			showCenteredSubtitle(player, "『宇宙热寂』");
			server.tickRateManager().setTickRate(20.0F);
			teleportToHeatDeath(player, server);
		}
	}

	/**
	 * 食用春秋肠时调用：清零计数器，还原被热寂维度暂存的主世界重生点；
	 * 若玩家当前位于 heat_death 维度，将其传送回主世界。
	 */
	public static void onChunqiuChang(ServerPlayer player) {
		player.setAttached(DRINK_COUNT, 0);
		boolean inHeatDeath = player.level().dimension().equals(HEAT_DEATH);

		ServerPlayer.RespawnConfig saved = player.getAttached(SAVED_RESPAWN);
		ServerPlayer.RespawnConfig current = player.getRespawnConfig();
		boolean respawnInHeatDeath = current != null && current.respawnData().dimension().equals(HEAT_DEATH);

		if (saved != null) {
			// 有暂存备份：还原主世界重生点
			player.setRespawnPosition(saved, true);
			player.removeAttached(SAVED_RESPAWN);
		} else if (respawnInHeatDeath || (inHeatDeath && player.getAttachedOrElse(TRAPPED, false))) {
			// 无备份（原重生点为空或旧档迁移）但曾/正被热寂锁定：清空热寂重生点，
			// 回到主世界默认出生点
			player.setRespawnPosition(null, false);
		}
		player.setAttached(TRAPPED, false);

		if (!inHeatDeath) {
			return;
		}
		MinecraftServer server = ((ServerLevel) player.level()).getServer();
		ServerLevel overworld = server.getLevel(Level.OVERWORLD);
		if (overworld == null) {
			return;
		}
		LevelData.RespawnData spawn = overworld.getRespawnData();
		BlockPos spawnPos = spawn.pos();
		player.teleportTo(overworld, spawnPos.getX() + 0.5, spawnPos.getY() + 0.1, spawnPos.getZ() + 0.5, Set.of(), spawn.yaw(), spawn.pitch(), false);
	}

	/**
	 * 将玩家传送到宇宙热寂维度并设置重生点（forced），使其死亡后只能在该维度重生。
	 * 设置热寂重生点前会暂存玩家当前（主世界）的重生点，避免覆写丢失。
	 */
	private static void teleportToHeatDeath(ServerPlayer player, MinecraftServer server) {
		ServerLevel heatDeathLevel = server.getLevel(HEAT_DEATH);
		if (heatDeathLevel == null) {
			return;
		}
		if (player.getRespawnConfig() != null) {
			player.setAttached(SAVED_RESPAWN, player.getRespawnConfig());
		}
		player.setAttached(TRAPPED, true);
		player.teleportTo(heatDeathLevel, 0.5, 1.0, 0.5, Set.of(), player.getYRot(), player.getXRot(), false);
		LevelData.RespawnData respawnData = LevelData.RespawnData.of(HEAT_DEATH, HEAT_DEATH_RESPAWN_POS, player.getYRot(), player.getXRot());
		player.setRespawnPosition(new ServerPlayer.RespawnConfig(respawnData, true), true);
	}

	/**
	 * 在屏幕中央显示字幕：先设置空标题触发标题计时，再设置字幕文本。
	 */
	private static void showCenteredSubtitle(ServerPlayer player, String text) {
		player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(text)));
		player.connection.send(new ClientboundSetTitleTextPacket(Component.empty()));
	}

	/** 供调试/其它逻辑读取当前计数。 */
	public static int getCount(AttachmentTarget target) {
		Integer count = target.getAttached(DRINK_COUNT);
		return count == null ? 0 : count;
	}

	/**
	 * 判断第 4 次饮用是否处于冷却中：仅当已饮 3 次且距离第 3 次饮用未满
	 * {@link #FOURTH_DRINK_COOLDOWN_SECONDS} 秒（现实世界时间）时为 true。
	 */
	public static boolean isFourthDrinkOnCooldown(ServerPlayer player) {
		if (getCount(player) != 3) {
			return false;
		}
		long end = player.getAttachedOrElse(FOURTH_DRINK_COOLDOWN_END, 0L);
		return end > Util.getMillis();
	}

	/** 第 4 次饮用冷却的剩余秒数（冷却中为 >0，否则为 0）。 */
	public static long fourthDrinkCooldownSecondsLeft(ServerPlayer player) {
		if (getCount(player) != 3) {
			return 0L;
		}
		long end = player.getAttachedOrElse(FOURTH_DRINK_COOLDOWN_END, 0L);
		long left = (end - Util.getMillis() + 999L) / 1000L;
		return Math.max(0L, left);
	}
}
