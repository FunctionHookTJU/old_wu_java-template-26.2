package functionhook.oldwu.client.animation;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * maodie 专用动画（Blockbench maodie.attack.json 导出，改写为 26.2 适用的 Java 代码）。
 *
 * <p>{@link #MAODIE_ATTACK} 为单手挥击的一次性动画，近战攻击与发射纸筒时播放。
 */
public final class MaodieAnimations {
	// 动画时长（毫秒）：0.7083 秒
	public static final float ATTACK_DURATION_MS = 708.0F;

	public static final AnimationDefinition MAODIE_ATTACK = AnimationDefinition.Builder.withLength(0.7083F)
		.addAnimation("handR", new AnimationChannel(AnimationChannel.Targets.ROTATION,
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(0.125F, KeyframeAnimations.degreeVec(8.3954F, -33.3955F, 42.7544F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(0.4167F, KeyframeAnimations.degreeVec(81.1216F, -34.2638F, 8.5652F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
		.addAnimation("handR", new AnimationChannel(AnimationChannel.Targets.POSITION,
			new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(0.125F, KeyframeAnimations.posVec(-6.0F, 1.0F, 2.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(0.4167F, KeyframeAnimations.posVec(-5.0F, 5.0F, 9.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
		.build();

	private MaodieAnimations() {
	}
}
