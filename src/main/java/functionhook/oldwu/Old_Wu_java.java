package functionhook.oldwu;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.feline.Cat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import functionhook.oldwu.advancement.MaodieObservationTracker;
import functionhook.oldwu.attribute.ModAttributes;
import functionhook.oldwu.audio.CatAudio;
import functionhook.oldwu.block.ModBlocks;
import functionhook.oldwu.cat.CatMatingLogic;
import functionhook.oldwu.cat.GoodCatLogic;
import functionhook.oldwu.cat.MaodieLogic;
import functionhook.oldwu.effect.ModEffects;
import functionhook.oldwu.entity.ModEntityTypes;
import functionhook.oldwu.item.GounaiDrinkTracker;
import functionhook.oldwu.item.ModItems;
import functionhook.oldwu.particle.ModParticles;
import functionhook.oldwu.sound.ModSounds;

public class Old_Wu_java implements ModInitializer {
	public static final String MOD_ID = "old_wu_java";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ModParticles.init();
		ModSounds.initialize();
		ModEntityTypes.init();
		ModBlocks.initialize();
		ModItems.initialize();
		ModAttributes.initialize();
		ModEffects.initialize();
		GounaiDrinkTracker.initialize();
		MaodieObservationTracker.initialize();
		registerEntityCleanup();
		registerShovelInteract();
		LOGGER.info("Hello Fabric world!");
	}

	// 用铲子右键猫会立即将其压扁（flat）。
	// 客户端返回 SUCCESS 触发正常的使用动作（挥臂），服务端进入 flat 并播放铁砧音效。
	// 26.3 移除了 ShovelItem 等工具类（工具改为纯数据组件），故改用原版 #minecraft:shovels 物品标签判定。
	private static void registerShovelInteract() {
		UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
			if (!(entity instanceof Cat cat) || !player.getItemInHand(hand).is(ItemTags.SHOVELS)) {
				return InteractionResult.PASS;
			}

			if (!level.isClientSide()) {
				CatMatingLogic.enterFlat(cat);
				// 铲子压扁：坏猫/键帽 50% 概率好猫值 +1
				if (GoodCatLogic.getGoodValue(cat) < GoodCatLogic.BAD_THRESHOLD && cat.getRandom().nextBoolean()) {
					GoodCatLogic.setGoodValue(cat, GoodCatLogic.getGoodValue(cat) + 1);
				}
				level.playSound(null, cat, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
			}
			return InteractionResult.SUCCESS;
		});
	}

	/**
	 * 实体卸载/移除时的清理：删除音频节流记录、移除残留的 Boss 血条。
	 *
	 * <p>Boss 血条原先只在 {@code die()} 时清理，若耄耋是被卸载（玩家走远、区块卸载、换维度）
	 * 而不是被打死，血条会一直挂在客户端屏幕上直到重登；这里补上这条生命周期路径。
	 * <p>音频节流表 {@link CatAudio} 以猫的 UUID 为键，若不清理由此只增不减。
	 */
	private static void registerEntityCleanup() {
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
			if (entity instanceof Cat cat) {
				CatAudio.forget(cat);
				MaodieLogic.removeBossBar(cat);
			}
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
