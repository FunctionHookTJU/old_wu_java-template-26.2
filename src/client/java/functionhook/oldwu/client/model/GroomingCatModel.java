package functionhook.oldwu.client.model;

import functionhook.oldwu.Old_Wu_java;
import functionhook.oldwu.cat.CatState;
import functionhook.oldwu.client.animation.CatAnimations;
import functionhook.oldwu.client.render.CatStateCarrier;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.CatRenderState;

/**
 * 愤怒状态模型（Blockbench 导出后适配 26.2 渲染 API）。
 * 姿态（弓背、低吼等）已烘焙在几何体中，这里只做轻微动态。
 */
public class GroomingCatModel extends EntityModel<CatRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Old_Wu_java.id("cat_grooming"), "main");

	private final KeyframeAnimation grooming;

	public GroomingCatModel(ModelPart root) {
		super(root);
		this.grooming = CatAnimations.GROOMING.bake(root);
	}

	@Override
	public void setupAnim(CatRenderState state) {
		super.setupAnim(state);
		if (state instanceof CatStateCarrier carrier && carrier.oldwu_getStateId() == CatState.GROOMING.ordinal()) {
			long time = (long) (state.ageInTicks * 50.0F);
			this.grooming.apply(time, 1.0F);
		}
	}
}
