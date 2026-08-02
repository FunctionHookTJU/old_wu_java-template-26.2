package functionhook.oldwu.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import functionhook.oldwu.cat.CatState;
import functionhook.oldwu.client.render.CatStateCarrier;
import functionhook.oldwu.client.render.CatStateModelHolder;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
	@Shadow
	protected M model;

	@Inject(method = "submit", at = @At("HEAD"))
	@SuppressWarnings("unchecked")
	private void oldwu_swapStateModel(
		S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci
	) {
		if (!((Object) this instanceof CatRenderer) || !(state instanceof CatStateCarrier carrier)) {
			return;
		}

		int stateId = carrier.oldwu_getStateId();
		boolean isBaby = state.isBaby;
		CatStateModelHolder holder = (CatStateModelHolder) (Object) this;
		EntityModel<?> stateModel = null;
		if (stateId == CatState.ANGRY.ordinal() || stateId == CatState.PAIRING.ordinal()) {
			stateModel = isBaby ? holder.oldwu_getAngryBabyModel() : holder.oldwu_getAngryModel();
		} else if (stateId == CatState.BATTLE.ordinal()) {
			stateModel = isBaby ? holder.oldwu_getBattleBabyModel() : holder.oldwu_getBattleModel();
		} else if (stateId == CatState.RECOVERY.ordinal()) {
			stateModel = isBaby ? holder.oldwu_getRecoveryBabyModel() : holder.oldwu_getRecoveryModel();
		} else if (stateId == CatState.FLAT.ordinal()) {
			stateModel = isBaby ? holder.oldwu_getFlatBabyModel() : holder.oldwu_getFlatModel();
		}

		if (stateModel != null) {
			this.model = (M) (Object) stateModel;
		}
	}
}
