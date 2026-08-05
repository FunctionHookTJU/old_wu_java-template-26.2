package functionhook.oldwu.client.model;

import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.CatRenderState;

import functionhook.oldwu.Old_Wu_java;
import functionhook.oldwu.cat.CatState;
import functionhook.oldwu.client.animation.CatAnimations;
import functionhook.oldwu.client.render.CatStateCarrier;

/**
 * 舔毛状态模型（Blockbench tianmao.java 导出，64×32），应用 grooming.json 动画。
 */
public class GroomingCatModel extends EntityModel<CatRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Old_Wu_java.id("cat_grooming"), "main");

	private final ModelPart body;
	private final KeyframeAnimation grooming;

	public GroomingCatModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.grooming = CatAnimations.GROOMING.bake(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 17.0F, 1.0F));

		body.addOrReplaceChild(
			"belly",
			CubeListBuilder.create()
				.texOffs(20, 0).addBox(-2.0F, -8.0F, -3.0F, 4.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(20, 11).addBox(-2.0F, 3.0F, -3.0F, 4.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(20, 6).addBox(-2.0F, -2.0F, -3.0F, 4.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F)
		);

		body.addOrReplaceChild(
			"head",
			CubeListBuilder.create()
				.texOffs(0, 0).addBox(-2.5F, -2.0F, -3.0F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(0, 24).addBox(-1.5F, -0.0156F, -4.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 10).addBox(-2.0F, -3.0F, 0.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(6, 10).addBox(1.0F, -3.0F, 0.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, -2.0F, -10.0F)
		);

		PartDefinition tail1 = body.addOrReplaceChild(
			"tail1",
			CubeListBuilder.create().texOffs(0, 15).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, -2.0F, 7.0F, 0.7854F, 0.0F, 0.0F)
		);
		tail1.addOrReplaceChild(
			"tail2",
			CubeListBuilder.create().texOffs(4, 15).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, 0.7854F, 0.0F, 0.0F)
		);

		body.addOrReplaceChild(
			"backLegL",
			CubeListBuilder.create().texOffs(8, 13).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offset(1.1F, 1.0F, 6.0F)
		);
		body.addOrReplaceChild(
			"backLegR",
			CubeListBuilder.create().texOffs(8, 13).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offset(-1.1F, 1.0F, 6.0F)
		);
		body.addOrReplaceChild(
			"frontLegL",
			CubeListBuilder.create().texOffs(40, 0).addBox(-1.0F, -0.2F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offset(1.2F, -3.0F, -5.0F)
		);
		body.addOrReplaceChild(
			"frontLegR",
			CubeListBuilder.create().texOffs(40, 0).addBox(-1.0F, -0.2F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offset(-1.2F, -3.0F, -5.0F)
		);

		return LayerDefinition.create(meshdefinition, 64, 32);
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
