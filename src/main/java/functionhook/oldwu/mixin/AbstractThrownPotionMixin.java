package functionhook.oldwu.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.phys.AABB;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import functionhook.oldwu.Old_Wu_java;
import functionhook.oldwu.cat.CatPartners;
import functionhook.oldwu.cat.CatState;

@Mixin(AbstractThrownPotion.class)
public abstract class AbstractThrownPotionMixin {
	@Inject(
		method = "onHitAsWater(Lnet/minecraft/server/level/ServerLevel;)V",
		at = @At("HEAD")
	)
	private void oldwu_triggerGrooming(ServerLevel level, CallbackInfo ci) {
		AbstractThrownPotion potion = (AbstractThrownPotion) (Object) this;
		AABB splashArea = potion.getBoundingBox().inflate(4.0, 2.0, 4.0);
		int triggeredCats = 0;

		for (Cat cat : level.getEntitiesOfClass(Cat.class, splashArea)) {
			if (potion.distanceToSqr(cat) >= 16.0) {
				continue;
			}

			CatPartners.setGroomingTimer(cat, 100);
			CatPartners.setState(cat, CatState.GROOMING);
			CatPartners.setPartner(cat, null);
			cat.getNavigation().stop();
			triggeredCats++;

			Old_Wu_java.LOGGER.info(
				"Water splash forced cat {} into GROOMING for 100 ticks",
				cat.getUUID()
			);
		}

		Old_Wu_java.LOGGER.info(
			"Water splash at [{}, {}, {}] triggered {} cat(s)",
			potion.getX(), potion.getY(), potion.getZ(), triggeredCats
		);
	}
}