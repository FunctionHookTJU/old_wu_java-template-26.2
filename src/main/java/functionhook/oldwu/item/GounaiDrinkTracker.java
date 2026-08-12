package functionhook.oldwu.item;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelData;

import functionhook.oldwu.Old_Wu_java;

/**
 * 野生狗奶的隐藏饮用计数器，以及宇宙热寂（heat_death）维度的配套逻辑。
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
 * 会依次显示三条字幕（各 2 秒现实时间）——"生命惧怕时间"、"而时间惧怕野生狗奶"、
 * "春秋肠，凌驾时间之上"——全部播完后才传送回主世界。
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

	/** 热寂维度玩家进入处的唯一基岩（0,0,0）。 */
	private static final BlockPos HEAT_DEATH_BEDROCK_POS = new BlockPos(0, 0, 0);

	/** 春秋肠字幕序列：玩家 UUID -> 序列状态（仅内存，无需持久化）。 */
	private static final Map<UUID, SubtitleSequence> CHUNQIU_SEQUENCES = new ConcurrentHashMap<>();

	/** 春秋肠在热寂维度依次显示的三条字幕。 */
	private static final String[] CHUNQIU_SUBTITLES = {
		"生命惧怕时间",
		"而时间惧怕野生狗奶",
		"春秋肠，凌驾时间之上"
	};

	/** 每条字幕显示的现实秒数。 */
	private static final long SUBTITLE_DISPLAY_SECONDS = 2L;

	private GounaiDrinkTracker() {
	}

	/**
	 * 注册服务端事件：热寂维度（0,0,0）基岩补位 + 春秋肠字幕序列推进。
	 */
	public static void initialize() {
		ServerChunkEvents.CHUNK_LOAD.register((serverLevel, chunk, newChunk) -> {
			if (!serverLevel.dimension().equals(HEAT_DEATH)) {
				return;
			}
			if (chunk.getPos().x() != 0 || chunk.getPos().z() != 0) {
				return;
			}
			if (serverLevel.getBlockState(HEAT_DEATH_BEDROCK_POS).isAir()) {
				serverLevel.setBlockAndUpdate(HEAT_DEATH_BEDROCK_POS, Blocks.BEDROCK.defaultBlockState());
			}
		});
		ServerTickEvents.END_SERVER_TICK.register(GounaiDrinkTracker::tickSequences);
	}

	private static void tickSequences(MinecraftServer server) {
		if (CHUNQIU_SEQUENCES.isEmpty()) {
			return;
		}
		CHUNQIU_SEQUENCES.values().removeIf(seq -> advanceSequence(seq, server));
	}

	/** 推进一条字幕序列：按现实时间每 2 秒显示下一条，全部播完后清空标题并传送回主世界。 */
	private static boolean advanceSequence(SubtitleSequence seq, MinecraftServer server) {
		ServerPlayer player = server.getPlayerList().getPlayer(seq.playerId);
		if (player == null || player.isRemoved()) {
			return true;
		}
		long elapsed = Util.getMillis() - seq.startMillis;
		int targetStep = (int) (elapsed / (SUBTITLE_DISPLAY_SECONDS * 1000L));
		while (targetStep > seq.step) {
			seq.step++;
			if (seq.step < CHUNQIU_SUBTITLES.length) {
				showCenteredSubtitle(player, CHUNQIU_SUBTITLES[seq.step]);
			} else {
				player.connection.send(new ClientboundClearTitlesPacket(false));
				teleportBackToOverworld(player, server);
				return true;
			}
		}
		return false;
	}

	/**
	 * 服务端调用：饮用野生狗奶后推进隐藏计数器并触发对应表现。
	 */
	public static void onDrink(ServerPlayer player) {
		int count = player.getAttachedOrElse(DRINK_COUNT, 0) + 1;
		player.setAttached(DRINK_COUNT, count);
		Old_Wu_java.LOGGER.info("[GounaiDrink] {} drank milk, count is now {}", player.getName().getString(), count);

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
	 * 若玩家当前位于 heat_death 维度，依次显示三条字幕（各 2 秒）后传送回主世界。
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
		// 依次显示三条字幕（各 2 秒现实时间），全部播完后再传送回主世界
		showCenteredSubtitle(player, CHUNQIU_SUBTITLES[0]);
		CHUNQIU_SEQUENCES.put(player.getUUID(), new SubtitleSequence(player));
	}

	/**
	 * 将玩家传送到宇宙热寂维度并设置重生点（forced），使其死亡后只能在该维度重生。
	 * 设置热寂重生点前会暂存玩家当前（主世界）的重生点，避免覆写丢失。
	 */
	private static void teleportToHeatDeath(ServerPlayer player, MinecraftServer server) {
		ServerLevel heatDeathLevel = server.getLevel(HEAT_DEATH);
		if (heatDeathLevel == null) {
			Old_Wu_java.LOGGER.error("[GounaiDrink] Heat death level {} not loaded, cannot teleport!", HEAT_DEATH);
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

	/** 传送玩家到主世界世界出生点。 */
	private static void teleportBackToOverworld(ServerPlayer player, MinecraftServer server) {
		ServerLevel overworld = server.getLevel(Level.OVERWORLD);
		if (overworld == null) {
			return;
		}
		LevelData.RespawnData spawn = overworld.getRespawnData();
		BlockPos spawnPos = spawn.pos();
		player.teleportTo(overworld, spawnPos.getX() + 0.5, spawnPos.getY() + 0.1, spawnPos.getZ() + 0.5, Set.of(), spawn.yaw(), spawn.pitch(), false);
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

	/** 春秋肠字幕序列状态。 */
	private static final class SubtitleSequence {
		private final UUID playerId;
		private final long startMillis;
		private int step;

		SubtitleSequence(ServerPlayer player) {
			this.playerId = player.getUUID();
			this.startMillis = Util.getMillis();
		}
	}
}
