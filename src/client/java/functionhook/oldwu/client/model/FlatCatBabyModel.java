package functionhook.oldwu.client.model;

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

/**
 * 压扁（flat）状态幼年模型（Blockbench 导出后适配 26.2 渲染 API，32×32）。
 */
public class FlatCatBabyModel extends EntityModel<CatRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Old_Wu_java.id("cat_flat_baby"), "main");

	private final ModelPart head;
	private final ModelPart tail1;

	public FlatCatBabyModel(ModelPart root) {
		super(root);
		this.head = root.getChild("body").getChild("head");
		this.tail1 = root.getChild("body").getChild("tail1");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 23.0F, 0.0F));

		body.addOrReplaceChild(
			"belly",
			CubeListBuilder.create().texOffs(-3, 8).addBox(-4.0F, 4.5F, -6.5F, 8.0F, 0.0F, 10.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, -3.5F, 0.5F)
		);

		body.addOrReplaceChild(
			"head",
			CubeListBuilder.create()
				.texOffs(0, 0).addBox(-2.5F, -2.0F, -2.875F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(18, 0).addBox(-2.0F, -3.0F, -0.875F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(24, 0).addBox(1.0F, -3.0F, -0.875F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(18, 3).addBox(-1.5F, 0.0F, -3.875F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, 0.0F, -7.125F)
		);

		PartDefinition tail1 = body.addOrReplaceChild(
			"tail1", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -4.893F, 3.9151F, -0.5672F, 0.0F, 0.0F)
		);

		tail1.addOrReplaceChild(
			"tail1_r1",
			CubeListBuilder.create().texOffs(0, 18).addBox(-0.5F, -2.0F, 4.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, 8.893F, 0.0849F, 0.5672F, 0.0F, 0.0F)
		);

		PartDefinition backLegL = body.addOrReplaceChild("backLegL", CubeListBuilder.create(), PartPose.offset(1.0F, -2.0F, 2.5F));
		backLegL.addOrReplaceChild(
			"backLegL_r1",
			CubeListBuilder.create().texOffs(17, 21).addBox(0.5F, -1.0F, 1.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(7.0F, 3.0F, -2.5F, 0.0F, -1.5708F, 0.0F)
		);

		PartDefinition backLegR = body.addOrReplaceChild("backLegR", CubeListBuilder.create(), PartPose.offset(-1.0F, -2.0F, 2.5F));
		backLegR.addOrReplaceChild(
			"backLegR_r1",
			CubeListBuilder.create().texOffs(11, 21).addBox(-1.5F, -1.0F, 1.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(-7.0F, 3.0F, -2.5F, 0.0F, 1.5708F, 0.0F)
		);

		PartDefinition frontLegL = body.addOrReplaceChild("frontLegL", CubeListBuilder.create(), PartPose.offset(-6.0F, -2.0F, -1.5F));
		frontLegL.addOrReplaceChild(
			"frontLegL_r1",
			CubeListBuilder.create().texOffs(17, 17).addBox(0.5F, -1.0F, -2.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(2.0F, 3.0F, -2.5F, 0.0F, 1.5708F, 0.0F)
		);

		PartDefinition frontLegR = body.addOrReplaceChild("frontLegR", CubeListBuilder.create(), PartPose.offset(-1.0F, -2.0F, -1.5F));
		frontLegR.addOrReplaceChild(
			"frontLegR_r1",
			CubeListBuilder.create().texOffs(11, 17).addBox(-1.5F, -1.0F, -2.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(5.0F, 3.0F, -2.5F, 0.0F, -1.5708F, 0.0F)
		);

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(CatRenderState state) {
		super.setupAnim(state);

		float age = state.ageInTicks;
		this.head.yRot += Mth.sin(age * 0.2F) * 0.03F;
		this.tail1.zRot += Mth.sin(age * 0.3F) * 0.05F;
	}
}
