package functionhook.oldwu.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import functionhook.oldwu.effect.EternalRemoval;
import functionhook.oldwu.effect.ModEffects;

/**
 * 永恒（Eternal）状态效果的服务端行为：
 * <ul>
 *   <li>{@code hurtServer}：拥有永恒效果的生物不受任何伤害（连 /kill 指令
 *       （内部走 {@code kill -> hurtServer(genericKill)}）也无法杀死）。</li>
 *   <li>{@code removeEffect}/{@code removeEffectNoUpdate}：永恒效果不可被单独移除
 *       （喝牛奶 {@code removeAllEffects}、喝蜂蜜 {@code removeEffect(POISON)} 等均无法解除）。</li>
 *   <li>{@code removeAllEffects}：牛奶等"清除全部效果"时保留永恒，其余效果正常清除。</li>
 *   <li>食用春秋肠可通过 {@link EternalRemoval#run} 临时放行移除，作为解除永恒的后门。</li>
 * </ul>
 */
@Mixin(LivingEntity.class)
public abstract class EternalEffectMixin {
	/**
	 * 拥有永恒效果的生物免疫所有伤害。
	 */
	@Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
	private void oldwu_eternalImmuneToDamage(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self.hasEffect(ModEffects.ETERNAL)) {
			cir.setReturnValue(false);
		}
	}

	/**
	 * 阻止单独移除永恒效果（蜂蜜 {@code RemoveStatusEffectsConsumeEffect} 走此路径）。
	 * 通过 {@link EternalRemoval} 放行时允许移除（春秋肠后门）。
	 * 客户端（镜像实体）始终放行，确保服务端移除后能同步清除本地效果。
	 */
	@Inject(method = "removeEffect", at = @At("HEAD"), cancellable = true)
	private void oldwu_eternalNotRemovable(Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (effect != ModEffects.ETERNAL || self.level().isClientSide()) {
			return;
		}
		if (!EternalRemoval.isAllowed()) {
			cir.setReturnValue(false);
		}
	}

	/**
	 * 底层 {@code removeEffectNoUpdate} 同样拦截，防止绕过 {@code removeEffect} 直接移除。
	 * 通过 {@link EternalRemoval} 放行时允许移除（春秋肠后门）。
	 * 客户端（镜像实体，含 {@code ClientPacketListener.handleRemoveMobEffect} 的同步路径）
	 * 始终放行，确保服务端移除后客户端能正确清除。
	 */
	@Inject(method = "removeEffectNoUpdate", at = @At("HEAD"), cancellable = true)
	private void oldwu_eternalNotRemovableNoUpdate(Holder<MobEffect> effect, CallbackInfoReturnable<MobEffectInstance> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (effect != ModEffects.ETERNAL || self.level().isClientSide()) {
			return;
		}
		if (!EternalRemoval.isAllowed()) {
			cir.setReturnValue(null);
		}
	}

	/**
	 * 喝牛奶（{@code removeAllEffects}）时：清除其它全部效果，但保留永恒。
	 * 通过 {@link EternalRemoval} 放行时（春秋肠后门）不保留永恒。
	 */
	@Inject(method = "removeAllEffects", at = @At("HEAD"), cancellable = true)
	private void oldwu_keepEternalInRemoveAll(CallbackInfoReturnable<Boolean> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!self.hasEffect(ModEffects.ETERNAL)) {
			return;
		}
		// 客户端 removeAllEffects 原实现直接返回 false，保持一致性
		if (self.level().isClientSide()) {
			cir.setReturnValue(false);
			return;
		}
		boolean removedAny = false;
		for (MobEffectInstance instance : List.copyOf(self.getActiveEffects())) {
			if (instance.is(ModEffects.ETERNAL) && !EternalRemoval.isAllowed()) {
				continue;
			}
			if (self.removeEffect(instance.getEffect())) {
				removedAny = true;
			}
		}
		cir.setReturnValue(removedAny);
	}

	@Inject(method = "kill", at = @At("HEAD"), cancellable = true)
	private void oldwu_eternalBlockKill(ServerLevel level, CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self.hasEffect(ModEffects.ETERNAL)) {
			ci.cancel();
		}
	}
}
