package functionhook.oldwu.accessor;

import net.minecraft.world.entity.ai.goal.GoalSelector;

/**
 * 由 {@code MobMixin} 实现，暴露 Mob 的 protected 字段 {@code targetSelector}。
 * 不能放在 mixin 包内，否则会触发 IllegalClassLoadError。
 */
public interface MobTargetAccessor {
	GoalSelector oldwu_getTargetSelector();
}
