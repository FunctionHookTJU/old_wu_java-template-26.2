package functionhook.oldwu.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


import functionhook.oldwu.advancement.MaodieAdvancements;
import functionhook.oldwu.attribute.ModAttributes;
import functionhook.oldwu.cat.CatMatingLogic;
import functionhook.oldwu.cat.MaodieLogic;
import functionhook.oldwu.item.ModItems;


@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	/**
	 * 狼的额外最大生命值：原版 {@code generic.max_health} 有 1024 上限，无法达到
	 * "大狗叫"喂食 64 次所需的生命值，故通过自定义属性 {@code extra_max_health} 叠加。
	 */
	@Inject(method = "getMaxHealth", at = @At("RETURN"), cancellable = true)
	private void oldwu_addExtraMaxHealth(CallbackInfoReturnable<Float> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self instanceof Wolf wolf) {
			AttributeInstance extra = wolf.getAttribute(ModAttributes.EXTRA_MAX_HEALTH);
			if (extra != null && extra.getValue() > 0.0) {
				cir.setReturnValue(cir.getReturnValue() + (float) extra.getValue());
			}
		}
	}

	/**
	 * maodie 被击败后随机掉落 2~5 个纸筒，掉落数量受抢夺附魔影响（每级抢夺额外 +0~1）。
	 */
	@Inject(method = "die", at = @At("TAIL"))
	private void oldwu_maodieDrops(DamageSource source, CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!(self instanceof Cat cat) || !CatMatingLogic.isMaodie(cat)) {
			return;
		}

		// 移除耄耋的 Boss 血条
		MaodieLogic.removeBossBar(cat);

		// 击杀耄耋的玩家获得进度
		if (source.getEntity() instanceof ServerPlayer player) {
			MaodieAdvancements.awardDefeatMaodie(player);
		}

		if (!(cat.level() instanceof ServerLevel serverLevel)) {
			return;
		}

		int looting = 0;
		if (source.getEntity() instanceof LivingEntity killer) {
			looting = EnchantmentHelper.getEnchantmentLevel(
				serverLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING),
				killer
			);
		}

		int count = 2 + cat.getRandom().nextInt(4);
		for (int i = 0; i < looting; i++) {
			if (cat.getRandom().nextBoolean()) {
				count++;
			}
		}

		for (int i = 0; i < count; i++) {
			cat.drop(new ItemStack(ModItems.PAPER_ROLL), false, false);
		}
	}
}

