package functionhook.oldwu.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import functionhook.oldwu.cat.CatMatingLogic;
import functionhook.oldwu.cat.MaodieLogic;
import functionhook.oldwu.item.ModItems;


@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
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

