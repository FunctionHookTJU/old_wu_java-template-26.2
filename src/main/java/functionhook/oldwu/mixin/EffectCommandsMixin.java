package functionhook.oldwu.mixin;

import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import functionhook.oldwu.effect.EternalRemoval;

/**
 * 让 {@code /effect clear}（清除全部或指定效果）能够解除"永恒"效果。
 *
 * <p>原版 {@code /effect clear} 走 {@code LivingEntity.removeAllEffects()} 或
 * {@code LivingEntity.removeEffect(...)}，默认会被 {@code EternalEffectMixin} 拦截
 * （以保留牛奶/蜂蜜无法解除永恒的设计）。这里通过 {@link EternalRemoval#run} 临时放行，
 * 使指令解除永恒时不被拦截，而牛奶/蜂蜜等其它路径仍保持拦截。
 */
@Mixin(net.minecraft.server.commands.EffectCommands.class)
public abstract class EffectCommandsMixin {
	@Redirect(
		method = "clearEffects",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;removeAllEffects()Z")
	)
	private static boolean oldwu_clearAllWithEternalRemoval(LivingEntity entity) {
		AtomicBoolean removed = new AtomicBoolean(false);
		EternalRemoval.run(() -> removed.set(entity.removeAllEffects()));
		return removed.get();
	}

	@Redirect(
		method = "clearEffect",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;removeEffect(Lnet/minecraft/core/Holder;)Z")
	)
	private static boolean oldwu_clearSpecificWithEternalRemoval(LivingEntity entity, Holder<MobEffect> effect) {
		AtomicBoolean removed = new AtomicBoolean(false);
		EternalRemoval.run(() -> removed.set(entity.removeEffect(effect)));
		return removed.get();
	}
}
