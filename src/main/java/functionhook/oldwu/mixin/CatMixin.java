package functionhook.oldwu.mixin;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import functionhook.oldwu.cat.CatMatingLogic;
import functionhook.oldwu.cat.CatPartners;

@Mixin(Cat.class)
public abstract class CatMixin {
	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	private void oldwu_definePartner(SynchedEntityData.Builder entityData, CallbackInfo ci) {
		entityData.define(CatPartners.PARTNER_UUID, CatPartners.NO_PARTNER);
	}

	@Inject(method = "customServerAiStep", at = @At("TAIL"))
	private void oldwu_mateLogic(ServerLevel level, CallbackInfo ci) {
		CatMatingLogic.tick((Cat) (Object) this);
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void oldwu_savePartner(ValueOutput output, CallbackInfo ci) {
		Cat self = (Cat) (Object) this;
		CatPartners.getPartner(self).ifPresent(uuid -> output.store("oldwu_partner", UUIDUtil.CODEC, uuid));
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void oldwu_loadPartner(ValueInput input, CallbackInfo ci) {
		Cat self = (Cat) (Object) this;
		input.read("oldwu_partner", UUIDUtil.CODEC).ifPresent(uuid -> CatPartners.setPartner(self, uuid));
	}
}
