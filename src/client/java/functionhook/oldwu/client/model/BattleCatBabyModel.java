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
 * 战斗状态幼年模型（Blockbench 导出后适配 26.2 渲染 API，32×32）。
 */
public class BattleCatBabyModel extends EntityModel<CatRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Old_Wu_java.id("cat_battle_baby"), "main");

	private final ModelPart head;
	private final ModelPart tail1;

	public BattleCatBabyModel(ModelPart root) {
		super(root);
		this.head = root.getChild("body").getChild("head");
		this.tail1 = root.getChild("body").getChild("tail1");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild(
			"body", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0F, 23.0F, 0.0F, 0.0F, 0.0F, -1.4399F)
		);

		body.addOrReplaceChild(
			"belly",
			CubeListBuilder.create().texOffs(0, 8).addBox(-2.0F, -1.5F, -3.5F, 4.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, -3.5F, 0.5F)
		);

		body.addOrReplaceChild(
			"head",
			CubeListBuilder.create()
				.texOffs(0, 0).addBox(-2.5F, -3.0F, -2.875F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(18, 0).addBox(-2.0F, -4.0F, -0.875F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(24, 0).addBox(1.0F, -4.0F, -0.875F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(18, 3).addBox(-1.5F, -1.0F, -3.875F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, -2.0F, -4.125F)
		);

		body.addOrReplaceChild(
			"tail1",
			CubeListBuilder.create().texOffs(0, 18).addBox(-0.5F, -0.107F, 0.0849F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, -4.893F, 3.9151F, -0.5672F, 0.0F, 0.0F)
		);

		body.addOrReplaceChild(
			"backLegL",
			CubeListBuilder.create().texOffs(18, 22).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offset(1.0F, -2.0F, 2.5F)
		);

		body.addOrReplaceChild(
			"backLegR",
			CubeListBuilder.create().texOffs(12, 22).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offset(-1.0F, -2.0F, 2.5F)
		);

		body.addOrReplaceChild(
			"frontLegL",
			CubeListBuilder.create().texOffs(18, 18).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offset(1.0F, -2.0F, -1.5F)
		);

		body.addOrReplaceChild(
			"frontLegR",
			CubeListBuilder.create().texOffs(12, 18).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offset(-1.0F, -2.0F, -1.5F)
		);

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(CatRenderState state) {
		super.setupAnim(state);

		float age = state.ageInTicks;
		this.head.yRot += Mth.sin(age * 0.5F) * 0.16F;
		this.head.xRot += Mth.sin(age * 0.35F) * 0.08F;
		this.tail1.zRot += Mth.sin(age * 0.8F) * 0.24F;
	}
}
