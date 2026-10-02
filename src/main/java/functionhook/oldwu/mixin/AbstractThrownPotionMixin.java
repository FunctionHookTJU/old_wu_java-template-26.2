package functionhook.oldwu.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.AABB;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import functionhook.oldwu.Old_Wu_java;
import functionhook.oldwu.cat.CatMatingLogic;
import functionhook.oldwu.cat.CatPartners;
import functionhook.oldwu.cat.CatState;

@Mixin(AbstractThrownPotion.class)
public abstract class AbstractThrownPotionMixin {
	/**
	 * 水瓶溅射 → 范围内的猫进入舔毛（GROOMING）100 tick、和平 600 tick。
	 *
	 * <p>26.3 起原版把 {@code onHitAsWater(ServerLevel)} 删除，水的实体影响逻辑并入
	 * {@code affectEntitiesAround(ServerLevel, PotionContents)}。这里改挂该方法，并用
	 * 与 26.2 完全相同的判定（只处理水瓶 {@code Potions.WATER}）保持行为一比一。
	 */
	@Inject(
		method = "affectEntitiesAround(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/alchemy/PotionContents;)V",
		at = @At("HEAD")
	)
	private void oldwu_triggerGrooming(ServerLevel level, PotionContents contents, CallbackInfo ci) {
		if (!contents.is(Potions.WATER)) {
			return;
		}

		AbstractThrownPotion potion = (AbstractThrownPotion) (Object) this;
		AABB splashArea = potion.getBoundingBox().inflate(4.0, 2.0, 4.0);
		int triggeredCats = 0;

		for (Cat cat : level.getEntitiesOfClass(Cat.class, splashArea)) {
			if (potion.distanceToSqr(cat) >= 16.0) {
				continue;
			}

			// maodie 使用专属 Boss 逻辑，不应被水瓶的舔毛状态覆盖；
			// 否则其会卡在 GROOMING 而无法退出（maodie 的 tick 分支直接返回）。
			if (CatMatingLogic.isMaodie(cat)) {
				continue;
			}

			CatPartners.setGroomingTimer(cat, 100);
			CatPartners.setBattlePeaceTimer(cat, 600);
			CatPartners.setState(cat, CatState.GROOMING);
			CatPartners.setPartner(cat, null);
			// 进入舔毛状态应取消坐下，否则坐下猫会被 tick 早期坐下分支拦截而卡在 GROOMING。
			cat.setOrderedToSit(false);
			cat.getNavigation().stop();
			triggeredCats++;

			Old_Wu_java.LOGGER.info(
					"Water splash forced cat {} into GROOMING for 100 ticks and peace for 600 ticks",
					cat.getUUID()
			);
		}

		Old_Wu_java.LOGGER.info(
			"Water splash at [{}, {}, {}] triggered {} cat(s)",
			potion.getX(), potion.getY(), potion.getZ(), triggeredCats
		);
	}
}