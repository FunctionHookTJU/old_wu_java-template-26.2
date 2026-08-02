package functionhook.oldwu.client.mixin;

import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.world.entity.animal.feline.Cat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import functionhook.oldwu.cat.CatPartners;
import functionhook.oldwu.client.model.AngryCatBabyModel;
import functionhook.oldwu.client.model.AngryCatModel;
import functionhook.oldwu.client.model.BattleCatBabyModel;
import functionhook.oldwu.client.model.BattleCatModel;
import functionhook.oldwu.client.model.FlatCatBabyModel;
import functionhook.oldwu.client.model.FlatCatModel;
import functionhook.oldwu.client.model.RecoveryCatBabyModel;
import functionhook.oldwu.client.model.RecoveryCatModel;
import functionhook.oldwu.client.render.CatStateCarrier;
import functionhook.oldwu.client.render.CatStateModelHolder;

@Mixin(CatRenderer.class)
public abstract class CatRendererMixin implements CatStateModelHolder {
	@Unique
	private AngryCatModel oldwuAngryModel;
	@Unique
	private AngryCatBabyModel oldwuAngryBabyModel;
	@Unique
	private BattleCatModel oldwuBattleModel;
	@Unique
	private BattleCatBabyModel oldwuBattleBabyModel;
	@Unique
	private RecoveryCatModel oldwuRecoveryModel;
	@Unique
	private RecoveryCatBabyModel oldwuRecoveryBabyModel;
	@Unique
	private FlatCatModel oldwuFlatModel;
	@Unique
	private FlatCatBabyModel oldwuFlatBabyModel;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void oldwu_bakeModels(EntityRendererProvider.Context context, CallbackInfo ci) {
		this.oldwuAngryModel = new AngryCatModel(context.bakeLayer(AngryCatModel.LAYER_LOCATION));
		this.oldwuAngryBabyModel = new AngryCatBabyModel(context.bakeLayer(AngryCatBabyModel.LAYER_LOCATION));
		this.oldwuBattleModel = new BattleCatModel(context.bakeLayer(BattleCatModel.LAYER_LOCATION));
		this.oldwuBattleBabyModel = new BattleCatBabyModel(context.bakeLayer(BattleCatBabyModel.LAYER_LOCATION));
		this.oldwuRecoveryModel = new RecoveryCatModel(context.bakeLayer(RecoveryCatModel.LAYER_LOCATION));
		this.oldwuRecoveryBabyModel = new RecoveryCatBabyModel(context.bakeLayer(RecoveryCatBabyModel.LAYER_LOCATION));
		this.oldwuFlatModel = new FlatCatModel(context.bakeLayer(FlatCatModel.LAYER_LOCATION));
		this.oldwuFlatBabyModel = new FlatCatBabyModel(context.bakeLayer(FlatCatBabyModel.LAYER_LOCATION));
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void oldwu_extractState(Cat entity, CatRenderState state, float partialTicks, CallbackInfo ci) {
		((CatStateCarrier) (Object) state).oldwu_setStateId(CatPartners.getState(entity).ordinal());
	}

	@Override
	public AngryCatModel oldwu_getAngryModel() {
		return this.oldwuAngryModel;
	}

	@Override
	public AngryCatBabyModel oldwu_getAngryBabyModel() {
		return this.oldwuAngryBabyModel;
	}

	@Override
	public BattleCatModel oldwu_getBattleModel() {
		return this.oldwuBattleModel;
	}

	@Override
	public BattleCatBabyModel oldwu_getBattleBabyModel() {
		return this.oldwuBattleBabyModel;
	}

	@Override
	public RecoveryCatModel oldwu_getRecoveryModel() {
		return this.oldwuRecoveryModel;
	}

	@Override
	public RecoveryCatBabyModel oldwu_getRecoveryBabyModel() {
		return this.oldwuRecoveryBabyModel;
	}

	@Override
	public FlatCatModel oldwu_getFlatModel() {
		return this.oldwuFlatModel;
	}

	@Override
	public FlatCatBabyModel oldwu_getFlatBabyModel() {
		return this.oldwuFlatBabyModel;
	}
}
