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
 * 回血状态模型（Blockbench 导出后适配 26.2 渲染 API）。
 * 姿态（弓背等）已烘焙在几何体中，这里只做轻微起伏动态。
 */
public class RecoveryCatModel extends EntityModel<CatRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Old_Wu_java.id("cat_recovery"), "main");

	private final ModelPart head;
	private final ModelPart tail1;
	private final ModelPart tail2;
	private final ModelPart backLegL;
	private final ModelPart backLegR;
	private final ModelPart frontLegL;
	private final ModelPart frontLegR;

	private final KeyframeAnimation spin;

	public RecoveryCatModel(ModelPart root) {
		super(root);
		this.spin = CatAnimations.SPIN.bake(root);
		this.head = root.getChild("body").getChild("head");
		this.tail1 = root.getChild("body").getChild("tail1");
		this.tail2 = this.tail1.getChild("tail2");
		this.backLegL = root.getChild("body").getChild("backLegL");
		this.backLegR = root.getChild("body").getChild("backLegR");
		this.frontLegL = root.getChild("body").getChild("frontLegL");
		this.frontLegR = root.getChild("body").getChild("frontLegR");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 17.0F, 1.0F));

		PartDefinition belly = body.addOrReplaceChild(
			"belly",
			CubeListBuilder.create().texOffs(20, 5).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F)
		);

		belly.addOrReplaceChild(
			"body3_r1",
			CubeListBuilder.create().texOffs(20, 11).addBox(-3.0F, -5.0F, -1.0F, 4.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(1.0F, 6.0F, -1.0F, -0.4363F, 0.0F, 0.0F)
		);

		belly.addOrReplaceChild(
			"body1_r1",
			CubeListBuilder.create().texOffs(20, 0).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, -2.0F, 2.0F, 0.3054F, 0.0F, 0.0F)
		);

		body.addOrReplaceChild(
			"head",
			CubeListBuilder.create()
				.texOffs(0, 0).addBox(-2.5F, -2.0F, -3.0F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(0, 24).addBox(-1.5F, -0.0156F, -4.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 10).addBox(-2.0F, -3.0F, 0.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(6, 10).addBox(1.0F, -3.0F, 0.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, -2.0F, -10.0F, 0.4914F, -0.2117F, -0.3793F)
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
			PartPose.offset(1.1F, 1.0F, 5.0F)
		);

		body.addOrReplaceChild(
			"backLegR",
			CubeListBuilder.create().texOffs(8, 13).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offset(-1.1F, 1.0F, 5.0F)
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


		float age = state.ageInTicks;

		this.head.yRot += Mth.sin(age * 0.2F) * 0.03F;
		this.head.xRot += Mth.sin(age * 0.15F) * 0.02F;

		this.tail1.zRot += Mth.sin(age * 0.3F) * 0.08F;
		this.tail2.zRot += Mth.sin(age * 0.3F + 0.5F) * 0.08F;

		if (state instanceof CatStateCarrier carrier && carrier.oldwu_getStateId() == CatState.DANCE.ordinal()) {
			this.spin.apply((long) (state.ageInTicks * 50.0F), 1.0F);
		}
	}
}
