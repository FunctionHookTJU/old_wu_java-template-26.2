package functionhook.oldwu.accessor;

import java.util.Set;

import net.minecraft.world.entity.ai.goal.WrappedGoal;

/**
 * 为 Cat 暴露被 maodie 化时清空的原版 AI 目标（goalSelector/targetSelector）暂存位，
 * 供改名恢复时重新添加。字段随实体生命周期，避免跨实体残留。
 */
public interface AiGoalsHolder {
	Set<WrappedGoal> oldwu_getSavedAiGoals();

	void oldwu_setSavedAiGoals(Set<WrappedGoal> goals);

	Set<WrappedGoal> oldwu_getSavedAiTargetGoals();

	void oldwu_setSavedAiTargetGoals(Set<WrappedGoal> goals);
}
