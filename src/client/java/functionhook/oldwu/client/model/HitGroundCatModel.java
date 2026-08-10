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
 * 老吴撼地掌（成年）模型（laowuhitground.java 适配 26.2 渲染 API，64×32）。
 * 处于 HITGROUND 状态时播放 0.5s 一次性动画。
 */
public class HitGroundCatModel extends EntityModel<CatRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Old_Wu_java.id("cat_hitground"), "main");

	private final ModelPart body;
	private final KeyframeAnimation hit;

	public HitGroundCatModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.hit = HitGroundAnimations.LAOWU_HIT.bake(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 17.0F, 1.0F));

		PartDefinition belly = body.addOrReplaceChild("belly", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));
		belly.addOrReplaceChild("body_r1",
			CubeListBuilder.create().texOffs(20, 0).addBox(-2.0F, -15.0F, -2.0F, 4.0F, 16.0F, 6.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, 7.0F, -1.0F, 0.2182F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
			.texOffs(0, 0).addBox(-2.5F, -2.0F, -3.0F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
			.texOffs(0, 24).addBox(-1.5F, -0.0156F, -4.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
			.texOffs(0, 10).addBox(-2.0F, -3.0F, 0.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
			.texOffs(6, 10).addBox(1.0F, -3.0F, 0.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -10.0F));
		head.addOrReplaceChild("head_r1",
			CubeListBuilder.create().texOffs(0, 25).addBox(-1.5F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, 1.4844F, -3.0F, 0.5236F, 0.0F, 0.0F));

		PartDefinition tail1 = body.addOrReplaceChild("tail1",
			CubeListBuilder.create().texOffs(0, 15).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, -2.0F, 7.0F, 2.6616F, 0.0F, 0.0F));
		tail1.addOrReplaceChild("tail2",
			CubeListBuilder.create().texOffs(4, 15).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition backLegL = body.addOrReplaceChild("backLegL", CubeListBuilder.create(), PartPose.offset(1.1F, 1.0F, 6.0F));
		backLegL.addOrReplaceChild("backLegL_r1",
			CubeListBuilder.create().texOffs(8, 13).addBox(0.1F, -6.0F, 6.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(1.9F, 8.0F, -3.0F, 0.1983F, 0.0916F, -0.4272F));

		PartDefinition backLegR = body.addOrReplaceChild("backLegR", CubeListBuilder.create(), PartPose.offset(-1.1F, 1.0F, 6.0F));
		backLegR.addOrReplaceChild("backLegR_r1",
			CubeListBuilder.create().texOffs(8, 13).addBox(-2.1F, -6.0F, 6.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(-1.9F, 8.0F, -3.0F, 0.1983F, -0.0916F, 0.4272F));

		PartDefinition frontLegL = body.addOrReplaceChild("frontLegL", CubeListBuilder.create(), PartPose.offset(1.2F, -3.0F, -5.0F));
		frontLegL.addOrReplaceChild("frontLegL_r1",
			CubeListBuilder.create().texOffs(40, 0).addBox(0.2F, -10.2F, -5.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(4.8F, 13.0F, -8.0F, -1.0036F, 0.0F, -0.6109F));

		PartDefinition frontLegR = body.addOrReplaceChild("frontLegR", CubeListBuilder.create(), PartPose.offset(-1.2F, -3.0F, -5.0F));
		frontLegR.addOrReplaceChild("frontLegR_r1",
			CubeListBuilder.create().texOffs(40, 0).addBox(-2.2F, -10.2F, -5.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(-4.8F, 13.0F, -8.0F, -1.0036F, 0.0F, 0.6109F));

		return LayerDefinition.create(meshdefinition, 64, 32);
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
