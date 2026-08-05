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
 * 战斗状态模型（Blockbench 导出后适配 26.2 渲染 API）。
 * battle 状态播放 BATTLE 关键帧动画；dance 状态播放 SPIN 旋转动画。
 */
public class BattleCatModel extends EntityModel<CatRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Old_Wu_java.id("cat_battle"), "main");

	private final KeyframeAnimation battle;
	private final KeyframeAnimation spin;

	public BattleCatModel(ModelPart root) {
		super(root);
		this.battle = CatAnimations.BATTLE.bake(root);
		this.spin = CatAnimations.SPIN.bake(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild(
			"body", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 21.0F, 1.0F, 0.0F, 0.0F, -1.4399F)
		);

		PartDefinition belly = body.addOrReplaceChild(
			"belly",
			CubeListBuilder.create().texOffs(20, 6).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
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

		if (state instanceof CatStateCarrier carrier) {
			int stateId = carrier.oldwu_getStateId();
			long time = (long) (state.ageInTicks * 50.0F);
			if (stateId == CatState.BATTLE.ordinal()) {
				this.battle.apply(time, 1.0F);
			} else if (stateId == CatState.DANCE.ordinal()) {
				this.spin.apply(time, 1.0F);
			}
		}
	}
}
