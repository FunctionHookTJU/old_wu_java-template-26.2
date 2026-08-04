package functionhook.oldwu.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

import functionhook.oldwu.Old_Wu_java;
import functionhook.oldwu.client.model.PaperRollModel;
import functionhook.oldwu.entity.PaperRoll;

public class PaperRollRenderer extends EntityRenderer<PaperRoll, PaperRollRenderState> {
	private static final Identifier TEXTURE = Old_Wu_java.id("textures/entity/paper_roll.png");

	private final PaperRollModel model;

	public PaperRollRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new PaperRollModel(context.bakeLayer(PaperRollModel.LAYER_LOCATION));
	}

	@Override
	protected int getBlockLightLevel(PaperRoll entity, BlockPos blockPos) {
		return 15;
	}

	@Override
	public PaperRollRenderState createRenderState() {
		return new PaperRollRenderState();
	}

	@Override
	public void extractRenderState(PaperRoll entity, PaperRollRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.yRot = entity.getYRot(partialTicks);
		state.xRot = entity.getXRot(partialTicks);
	}

	@Override
	public void submit(
		PaperRollRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera
	) {
		poseStack.pushPose();
		// 模型已居中于原点、长轴沿 Z，仅需 yaw/pitch 对齐飞行方向（参考 ArrowRenderer）
		poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
		poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot));
		poseStack.scale(0.5F, 0.5F, 0.5F);
		submitNodeCollector.submitModel(
			this.model, state, poseStack, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null
		);
		poseStack.popPose();
		super.submit(state, poseStack, submitNodeCollector, camera);
	}
}
