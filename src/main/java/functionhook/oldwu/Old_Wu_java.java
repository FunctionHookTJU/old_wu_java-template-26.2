package functionhook.oldwu;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.item.ShovelItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import functionhook.oldwu.advancement.MaodieObservationTracker;
import functionhook.oldwu.attribute.ModAttributes;
import functionhook.oldwu.block.ModBlocks;
import functionhook.oldwu.cat.CatMatingLogic;
import functionhook.oldwu.cat.GoodCatLogic;
import functionhook.oldwu.effect.ModEffects;
import functionhook.oldwu.entity.ModEntityTypes;
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
		MaodieObservationTracker.initialize();
		registerShovelInteract();
		LOGGER.info("Hello Fabric world!");
	}

	// 用铲子右键猫会立即将其压扁（flat）。
	// 客户端返回 SUCCESS 触发正常的使用动作（挥臂），服务端进入 flat 并播放铁砧音效。
	private static void registerShovelInteract() {
		UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
			if (!(entity instanceof Cat cat) || !(player.getItemInHand(hand).getItem() instanceof ShovelItem)) {
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

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
