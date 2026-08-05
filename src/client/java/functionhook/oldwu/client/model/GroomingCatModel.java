package functionhook.oldwu.client.model;

import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.animal.feline.AdultCatModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.CatRenderState;

import functionhook.oldwu.cat.CatState;
import functionhook.oldwu.client.animation.CatAnimations;
import functionhook.oldwu.client.render.CatStateCarrier;

/**
 * 原版成年猫体型上的舔毛动画模型。
 */
public class GroomingCatModel extends AdultCatModel {
	private final KeyframeAnimation grooming;

	public GroomingCatModel(ModelPart root) {
		super(root);
		this.grooming = CatAnimations.GROOMING.bake(root);
	}

	@Override
	public void setupAnim(CatRenderState state) {
		super.setupAnim(state);
		if (state instanceof CatStateCarrier carrier
			&& carrier.oldwu_getStateId() == CatState.GROOMING.ordinal()) {
			long time = (long) (state.ageInTicks * 50.0F);
			this.grooming.apply(time, 1.0F);
		}
	}
}
