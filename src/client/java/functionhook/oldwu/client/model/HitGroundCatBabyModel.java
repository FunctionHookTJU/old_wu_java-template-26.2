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
import functionhook.oldwu.client.animation.HitGroundAnimations;
import functionhook.oldwu.client.render.CatStateCarrier;

/**
 * 老吴撼地掌（幼年）模型（laowuhitground_baby.java 适配 26.2 渲染 API，32×32）。
 * 处于 HITGROUND 状态时播放幼年 0.5s 一次性动画。
 */
public class HitGroundCatBabyModel extends EntityModel<CatRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Old_Wu_java.id("cat_hitground_baby"), "main");

	private final ModelPart body;
	private final KeyframeAnimation hit;

	public HitGroundCatBabyModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.hit = HitGroundAnimations.LAOWU_HIT_BABY.bake(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		body.addOrReplaceChild("belly",
			CubeListBuilder.create().texOffs(0, 8).addBox(-2.0F, -1.5F, -3.5F, 4.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, -3.5F, 0.5F, 0.3054F, 0.0F, 0.0F));

		body.addOrReplaceChild("head", CubeListBuilder.create()
			.texOffs(0, 0).addBox(-2.5F, -3.0F, -2.875F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
			.texOffs(18, 0).addBox(-2.0F, -4.0F, -0.875F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
			.texOffs(24, 0).addBox(1.0F, -4.0F, -0.875F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
			.texOffs(18, 3).addBox(-1.5F, -1.0F, -3.875F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, -3.125F));

		PartDefinition tail1 = body.addOrReplaceChild("tail1", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -4.893F, 3.9151F, -0.5672F, 0.0F, 0.0F));
		tail1.addOrReplaceChild("tail1_r1",
			CubeListBuilder.create().texOffs(0, 18).addBox(-0.5F, -0.5F, -2.5F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, -1.607F, -0.4151F, 1.1345F, 0.0F, 0.0F));

		body.addOrReplaceChild("backLegL",
			CubeListBuilder.create().texOffs(18, 22).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offset(1.0F, -2.0F, 2.5F));
		body.addOrReplaceChild("backLegR",
			CubeListBuilder.create().texOffs(12, 22).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offset(-1.0F, -2.0F, 2.5F));

		PartDefinition frontLegL = body.addOrReplaceChild("frontLegL", CubeListBuilder.create(), PartPose.offset(1.0F, -2.0F, -1.5F));
		frontLegL.addOrReplaceChild("frontLegL_r1",
			CubeListBuilder.create().texOffs(18, 18).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, 1.0F, -1.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition frontLegR = body.addOrReplaceChild("frontLegR", CubeListBuilder.create(), PartPose.offset(-1.0F, -2.0F, -1.5F));
		frontLegR.addOrReplaceChild("frontLegR_r1",
			CubeListBuilder.create().texOffs(12, 18).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, 1.0F, -1.0F, -0.4363F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(CatRenderState state) {
		super.setupAnim(state);

		if (state instanceof CatStateCarrier carrier) {
			int animTick = carrier.oldwu_getHitgroundAnimTick();
			if (animTick > 0) {
				long elapsedMs = (long) ((state.ageInTicks - animTick) * 50.0F);
				if (elapsedMs >= 0 && elapsedMs < HitGroundAnimations.HIT_DURATION_MS) {
					this.hit.apply(elapsedMs, 1.0F);
				}
			}
		}
	}
}
