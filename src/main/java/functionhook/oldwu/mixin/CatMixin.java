package functionhook.oldwu.mixin;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import functionhook.oldwu.cat.CatMatingLogic;
import functionhook.oldwu.cat.CatPartners;
import functionhook.oldwu.cat.CatState;

@Mixin(Cat.class)
public abstract class CatMixin {
	// 在 super()/Builder 构造之前触发 CatPartners 类加载，提前完成 accessor 注册
	@Inject(method = "<init>", at = @At("HEAD"))
	private static void oldwu_initAccessors(CallbackInfo ci) {
		CatPartners.initAccessors();
	}

	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	private void oldwu_definePartner(SynchedEntityData.Builder entityData, CallbackInfo ci) {
		entityData.define(CatPartners.PARTNER_UUID, CatPartners.NO_PARTNER);
		entityData.define(CatPartners.STATE, CatState.COMMON.ordinal());
		entityData.define(CatPartners.ATTACK_COOLDOWN, 0);
		entityData.define(CatPartners.FLAT_TIMER, 0);
		entityData.define(CatPartners.PAIRING_TIMER, 0);
		entityData.define(CatPartners.DANCE_MODEL_INDEX, 0);
		entityData.define(CatPartners.DANCE_TIMER, 0);
		entityData.define(CatPartners.MAODIE_HAQI_TIMER, 0);
		entityData.define(CatPartners.MAODIE_RAGE_COOLDOWN, 0);
		entityData.define(CatPartners.MAODIE_ANIM_TICK, 0);
		entityData.define(CatPartners.GROOMING_TIMER, 0);
		entityData.define(CatPartners.BATTLE_PEACE_TIMER, 0);
	}



	@Inject(method = "customServerAiStep", at = @At("TAIL"))
	private void oldwu_mateLogic(ServerLevel level, CallbackInfo ci) {
		CatMatingLogic.tick(level, (Cat) (Object) this);
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

	// 模组状态（愤怒/配对/战斗/回血/压扁）或被命名为 maodie 时，屏蔽原版猫音效
	private boolean oldwu_isSilent() {
		Cat self = (Cat) (Object) this;
		return CatPartners.getState(self) != CatState.COMMON || CatMatingLogic.isMaodie(self);
	}

	// 压扁或被命名为 maodie 时，不播放原版环境音
	@Inject(method = "getAmbientSound", at = @At("HEAD"), cancellable = true)
	private void oldwu_flatAmbient(CallbackInfoReturnable<SoundEvent> cir) {
		if (this.oldwu_isSilent()) {
			cir.setReturnValue(null);
		}
	}

	// 压扁或被命名为 maodie 时，不播放原版受伤音
	@Inject(method = "getHurtSound", at = @At("HEAD"), cancellable = true)
	private void oldwu_flatHurt(DamageSource source, CallbackInfoReturnable<SoundEvent> cir) {
		if (this.oldwu_isSilent()) {
			cir.setReturnValue(null);
		}
	}

	// 压扁或被命名为 maodie 时，不播放原版死亡音
	@Inject(method = "getDeathSound", at = @At("HEAD"), cancellable = true)
	private void oldwu_flatDeath(CallbackInfoReturnable<SoundEvent> cir) {
		if (this.oldwu_isSilent()) {
			cir.setReturnValue(null);
		}
	}
}
