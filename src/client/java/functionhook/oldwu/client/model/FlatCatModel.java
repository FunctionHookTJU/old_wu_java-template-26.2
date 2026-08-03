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
import net.minecraft.util.Mth;

import functionhook.oldwu.Old_Wu_java;
import functionhook.oldwu.cat.CatState;
import functionhook.oldwu.client.animation.CatAnimations;
import functionhook.oldwu.client.render.CatStateCarrier;

/**
 * 压扁（flat）状态模型（Blockbench 导出后适配 26.2 渲染 API）。
 */
public class FlatCatModel extends EntityModel<CatRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Old_Wu_java.id("cat_flat"), "main");

	private final ModelPart head;
	private final ModelPart tail2;

	private final KeyframeAnimation spin;

	public FlatCatModel(ModelPart root) {
		super(root);
		this.spin = CatAnimations.SPIN.bake(root);
		this.head = root.getChild("body").getChild("head");
		this.tail2 = root.getChild("body").getChild("tail1").getChild("tail2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 21.0F, 1.0F));

		body.addOrReplaceChild(
			"belly",
			CubeListBuilder.create().texOffs(20, 6).addBox(-5.0F, -8.0F, -3.0F, 10.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F)
		);

		body.addOrReplaceChild(
			"head",
			CubeListBuilder.create()
				.texOffs(0, 0).addBox(-2.5F, -1.0F, -3.0F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(0, 24).addBox(-1.5F, 0.9844F, -4.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 10).addBox(-2.0F, -2.0F, 0.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(6, 10).addBox(1.0F, -2.0F, 0.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, 1.0F, -10.0F)
		);

		PartDefinition tail1 = body.addOrReplaceChild(
			"tail1", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -2.0F, 7.0F, 0.7854F, 0.0F, 0.0F)
		);

		tail1.addOrReplaceChild(
			"tail1_r1",
			CubeListBuilder.create().texOffs(0, 15).addBox(-0.5F, -6.0F, -1.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, 8.0F, 2.0F, 0.7854F, 0.0F, 0.0F)
		);

		tail1.addOrReplaceChild(
			"tail2",
			CubeListBuilder.create().texOffs(4, 15).addBox(-0.5F, 3.0F, 0.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, 0.7854F, 0.0F, 0.0F)
		);

		PartDefinition backLegL = body.addOrReplaceChild("backLegL", CubeListBuilder.create(), PartPose.offset(1.1F, 1.0F, 5.0F));
		backLegL.addOrReplaceChild(
			"backLegL_r1",
			CubeListBuilder.create().texOffs(9, 13).addBox(1.1F, 8.0F, 6.0F, 1.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(-4.1F, 4.0F, -7.0F, 0.0F, 0.0F, -1.5708F)
		);

		PartDefinition backLegR = body.addOrReplaceChild("backLegR", CubeListBuilder.create(), PartPose.offset(-1.1F, 1.0F, 5.0F));
		backLegR.addOrReplaceChild(
			"backLegR_r1",
			CubeListBuilder.create().texOffs(9, 13).addBox(-1.1F, 8.0F, 6.0F, 1.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(4.1F, 3.0F, -7.0F, 0.0F, 0.0F, 1.5708F)
		);

		PartDefinition frontLegL = body.addOrReplaceChild("frontLegL", CubeListBuilder.create(), PartPose.offset(1.2F, -3.0F, -5.0F));
		frontLegL.addOrReplaceChild(
			"frontLegL_r1",
			CubeListBuilder.create().texOffs(41, 0).addBox(1.2F, 7.8F, -5.0F, 1.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(-4.2F, 8.0F, 4.0F, 0.0F, 0.0F, -1.5708F)
		);

		PartDefinition frontLegR = body.addOrReplaceChild("frontLegR", CubeListBuilder.create(), PartPose.offset(-1.2F, -3.0F, -5.0F));
		frontLegR.addOrReplaceChild(
			"frontLegR_r1",
			CubeListBuilder.create().texOffs(41, 0).addBox(-1.2F, 7.8F, -5.0F, 1.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(4.2F, 7.0F, 4.0F, 0.0F, 0.0F, 1.5708F)
		);

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void setupAnim(CatRenderState state) {
		super.setupAnim(state);


		float age = state.ageInTicks;
		this.head.yRot += Mth.sin(age * 0.2F) * 0.03F;
		this.tail2.zRot += Mth.sin(age * 0.3F) * 0.06F;

		if (state instanceof CatStateCarrier carrier && carrier.oldwu_getStateId() == CatState.DANCE.ordinal()) {
			this.spin.apply((long) (state.ageInTicks * 50.0F), 1.0F);
		}
	}
}
