package functionhook.oldwu.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CatCollarLayer;
import net.minecraft.client.renderer.entity.state.CatRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import functionhook.oldwu.cat.CatState;
import functionhook.oldwu.client.render.CatStateCarrier;

/**
 * 驯服猫的项圈层：原版项圈模型只与原始猫模型对齐，模组在非 COMMON 状态会替换模型，
 * 导致项圈位置错乱。这里在非 COMMON 状态或耄耋时隐藏项圈，避免错位。
 */
@Mixin(CatCollarLayer.class)
public abstract class CatCollarLayerMixin {
	@Inject(method = "submit", at = @At("HEAD"), cancellable = true)
	private void oldwu_hideCollarInCustomStates(
		PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
		CatRenderState state, float yRot, float xRot, CallbackInfo ci
	) {
		if (state instanceof CatStateCarrier carrier
			&& (carrier.oldwu_isMaodie() || carrier.oldwu_getStateId() != CatState.COMMON.ordinal())) {
			ci.cancel();
		}
	}
}
