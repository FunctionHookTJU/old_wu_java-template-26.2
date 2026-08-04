package functionhook.oldwu.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.ai.goal.GoalSelector;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import functionhook.oldwu.accessor.MobTargetAccessor;

@Mixin(Mob.class)
public abstract class MobMixin implements MobTargetAccessor {
	/**
	 * 清除历史版本 {@code setNoAi(true)} 在猫身上残留的 NoAI 标志。
	 *
	 * <p>maodie 不再在此处关闭 AI：其专属行为（攻击玩家、狂暴发射纸筒）由
	 * {@code MaodieLogic} 在 {@code customServerAiStep} 中驱动，同时清空原版 AI
	 * 目标，因此保留 {@code isEffectiveAi()} 为 true 以便导航/移动正常运作。
	 */
	@Inject(method = "isEffectiveAi", at = @At("HEAD"), cancellable = true)
	private void oldwu_clearLegacyNoAi(CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof Cat cat && cat.isNoAi()) {
			cat.setNoAi(false);
		}
	}

	@Accessor("targetSelector")
	@Override
	public abstract GoalSelector oldwu_getTargetSelector();
}
