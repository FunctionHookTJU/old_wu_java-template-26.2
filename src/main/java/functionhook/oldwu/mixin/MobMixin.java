package functionhook.oldwu.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import functionhook.oldwu.cat.CatMatingLogic;
import functionhook.oldwu.cat.CatPartners;
import functionhook.oldwu.cat.CatState;

@Mixin(Mob.class)
public abstract class MobMixin {
	/**
	 * 被命名为 "maodie" 的猫移除全部 AI 与行为逻辑。
	 *
	 * <p>不使用 {@code setNoAi}（其会调用 {@code brain.removeAllBehaviors()} 永久清空行为，
	 * 改名后无法恢复），而是直接让 {@code isEffectiveAi()} 返回 false，使
	 * {@code serverAiStep()} 不再运行；改名后自动恢复原版 AI 与运行逻辑。
	 */
	@Inject(method = "isEffectiveAi", at = @At("HEAD"), cancellable = true)
	private void oldwu_maodieNoAi(CallbackInfoReturnable<Boolean> cir) {
		if (!((Object) this instanceof Cat cat)) {
			return;
		}

		if (CatMatingLogic.isMaodie(cat)) {
			CatPartners.setPartner(cat, null);
			CatPartners.setState(cat, CatState.COMMON);
			cat.getNavigation().stop();
			Vec3 motion = cat.getDeltaMovement();
			cat.setDeltaMovement(motion.x * 0.2, Math.max(motion.y, 0.0), motion.z * 0.2);
			cir.setReturnValue(false);
		} else if (cat.isNoAi()) {
			// 清除历史版本 setNoAi(true) 残留的 NoAI 标志，改名后恢复原版 AI
			cat.setNoAi(false);
		}
	}
}
