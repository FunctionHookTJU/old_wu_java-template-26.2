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
import functionhook.oldwu.client.animation.MaodieAnimations;
import functionhook.oldwu.client.render.CatStateCarrier;

/**
 * 被命名为 "maodie" 的猫使用的静态模型（Blockbench maodie.java 导出后适配 26.2 渲染 API，32×32）。
 * 近战攻击/发射纸筒时播放挥击动画。
 */
public class MaodieCatModel extends EntityModel<CatRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Old_Wu_java.id("cat_maodie"), "main");

	private final ModelPart bone;
	private final KeyframeAnimation attack;

	public MaodieCatModel(ModelPart root) {
		super(root);
		this.bone = root.getChild("bone");
		this.attack = MaodieAnimations.MAODIE_ATTACK.bake(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild(
			"bone",
			CubeListBuilder.create().texOffs(0, 25).addBox(-2.0F, -5.0F, 4.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, -3.1416F, 0.0F, 3.1416F)
		);

		bone.addOrReplaceChild(
			"body_r1",
			CubeListBuilder.create().texOffs(16, 11).addBox(-3.0F, -10.0F, -1.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(1.0F, -1.0F, -4.0F, -0.4363F, 0.0F, 0.0F)
		);

		PartDefinition leg_L = bone.addOrReplaceChild(
			"leg_L",
			CubeListBuilder.create().texOffs(14, 12).addBox(-3.0F, -1.0F, -1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, 0.0F, 0.0F)
		);
		leg_L.addOrReplaceChild(
			"leg_r1",
			CubeListBuilder.create().texOffs(20, 16).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(-2.0F, 0.0F, -2.0F, -0.48F, 0.0F, 0.0F)
		);

		PartDefinition leg_R = bone.addOrReplaceChild(
			"leg_R",
			CubeListBuilder.create().texOffs(0, 0).addBox(2.0F, -1.0F, -1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, 0.0F, 0.0F)
		);
		leg_R.addOrReplaceChild(
			"leg_r2",
			CubeListBuilder.create().texOffs(13, 11).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(2.0F, 0.0F, -2.0F, -0.48F, 0.0F, 0.0F)
		);

		PartDefinition tail = bone.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		tail.addOrReplaceChild(
			"cube_r1",
			CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -1.0F, -1.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(-5.0F, 0.0F, -4.0F, 0.0F, 0.2182F, 0.0F)
		);
		tail.addOrReplaceChild(
			"cube_r2",
			CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -1.0F, -1.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(-4.0F, 0.0F, -5.0F, 0.0F, 1.8326F, 0.0F)
		);
		tail.addOrReplaceChild(
			"cube_r3",
			CubeListBuilder.create().texOffs(1, 0).addBox(0.0F, -1.0F, -1.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, -1.0F, -6.0F, 0.6981F, 0.0F, 0.0F)
		);

		PartDefinition handR = bone.addOrReplaceChild("handR", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		handR.addOrReplaceChild(
			"cube_r4",
			CubeListBuilder.create().texOffs(14, 13).addBox(0.0F, -3.0F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(-3.0F, -5.0F, 2.0F, 0.0F, 0.0F, 0.2182F)
		);
		handR.addOrReplaceChild(
			"cube_r5",
			CubeListBuilder.create().texOffs(19, 22).addBox(0.0F, -5.0F, -1.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(-2.0F, 0.0F, 4.0F, 0.3442F, 0.0594F, -0.1642F)
		);

		PartDefinition handL = bone.addOrReplaceChild("handL", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		handL.addOrReplaceChild(
			"cube_r6",
			CubeListBuilder.create().texOffs(20, 14).addBox(-1.0F, -3.0F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(3.0F, -5.0F, 2.0F, 0.0F, 0.0F, -0.2182F)
		);
		handL.addOrReplaceChild(
			"cube_r7",
			CubeListBuilder.create().texOffs(13, 20).addBox(0.0F, -5.0F, -1.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(1.0F, 0.0F, 4.0F, 0.3442F, -0.0594F, 0.1642F)
		);

		bone.addOrReplaceChild(
			"head",
			CubeListBuilder.create().texOffs(15, 24).addBox(-2.0F, -13.0F, 0.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, 0.0F, 0.0F)
		);

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(CatRenderState state) {
		super.setupAnim(state);

		if (state instanceof CatStateCarrier carrier) {
			int animTick = carrier.oldwu_getMaodieAnimTick();
			if (animTick > 0) {
				long elapsedMs = (long) ((state.ageInTicks - animTick) * 50.0F);
				if (elapsedMs >= 0 && elapsedMs < MaodieAnimations.ATTACK_DURATION_MS) {
					this.attack.apply(elapsedMs, 1.0F);
				}
			}
		}
	}
}
