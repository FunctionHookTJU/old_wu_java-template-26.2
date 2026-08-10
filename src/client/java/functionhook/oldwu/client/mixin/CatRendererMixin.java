package functionhook.oldwu.client.mixin;

import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.feline.Cat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import functionhook.oldwu.Old_Wu_java;
import functionhook.oldwu.cat.CatMatingLogic;
import functionhook.oldwu.cat.CatPartners;
import functionhook.oldwu.cat.CatState;
import functionhook.oldwu.cat.MaodieLogic;
import functionhook.oldwu.client.model.AngryCatBabyModel;
import functionhook.oldwu.client.model.AngryCatModel;
import functionhook.oldwu.client.model.BattleCatBabyModel;
import functionhook.oldwu.client.model.BattleCatModel;
import functionhook.oldwu.client.model.FlatCatBabyModel;
import functionhook.oldwu.client.model.FlatCatModel;
import functionhook.oldwu.client.model.MaodieCatModel;
import functionhook.oldwu.client.model.RecoveryCatBabyModel;
import functionhook.oldwu.client.model.RecoveryCatModel;
import functionhook.oldwu.client.render.CatStateCarrier;
import functionhook.oldwu.client.render.CatStateModelHolder;
import functionhook.oldwu.client.model.GroomingCatModel;
import functionhook.oldwu.client.model.GroomingCatBabyModel;
import functionhook.oldwu.client.model.HitGroundCatModel;
import functionhook.oldwu.client.model.HitGroundCatBabyModel;

@Mixin(CatRenderer.class)
	public abstract class CatRendererMixin implements CatStateModelHolder {
	private static final Identifier MAODIE_TEXTURE = Old_Wu_java.id("textures/entity/maodie.png");
	private static final Identifier HAQI_TEXTURE = Old_Wu_java.id("textures/entity/haqi.png");

	@Unique
	private MaodieCatModel oldwuMaodieModel;
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
	@Unique
	private GroomingCatModel oldwuGroomingModel;
	@Unique
	private GroomingCatBabyModel oldwuGroomingBabyModel;
	@Unique
	private HitGroundCatModel oldwuHitGroundModel;
	@Unique
	private HitGroundCatBabyModel oldwuHitGroundBabyModel;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void oldwu_bakeModels(EntityRendererProvider.Context context, CallbackInfo ci) {
		this.oldwuMaodieModel = new MaodieCatModel(context.bakeLayer(MaodieCatModel.LAYER_LOCATION));
		this.oldwuAngryModel = new AngryCatModel(context.bakeLayer(AngryCatModel.LAYER_LOCATION));
		this.oldwuAngryBabyModel = new AngryCatBabyModel(context.bakeLayer(AngryCatBabyModel.LAYER_LOCATION));
		this.oldwuBattleModel = new BattleCatModel(context.bakeLayer(BattleCatModel.LAYER_LOCATION));
		this.oldwuBattleBabyModel = new BattleCatBabyModel(context.bakeLayer(BattleCatBabyModel.LAYER_LOCATION));
		this.oldwuRecoveryModel = new RecoveryCatModel(context.bakeLayer(RecoveryCatModel.LAYER_LOCATION));
		this.oldwuRecoveryBabyModel = new RecoveryCatBabyModel(context.bakeLayer(RecoveryCatBabyModel.LAYER_LOCATION));
		this.oldwuFlatModel = new FlatCatModel(context.bakeLayer(FlatCatModel.LAYER_LOCATION));
		this.oldwuFlatBabyModel = new FlatCatBabyModel(context.bakeLayer(FlatCatBabyModel.LAYER_LOCATION));
		this.oldwuGroomingModel = new GroomingCatModel(context.bakeLayer(GroomingCatModel.LAYER_LOCATION));
		this.oldwuGroomingBabyModel = new GroomingCatBabyModel(context.bakeLayer(GroomingCatModel.LAYER_LOCATION));
		this.oldwuHitGroundModel = new HitGroundCatModel(context.bakeLayer(HitGroundCatModel.LAYER_LOCATION));
		this.oldwuHitGroundBabyModel = new HitGroundCatBabyModel(context.bakeLayer(HitGroundCatBabyModel.LAYER_LOCATION));
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void oldwu_extractState(Cat entity, CatRenderState state, float partialTicks, CallbackInfo ci) {
		boolean maodie = CatMatingLogic.isMaodie(entity);
		boolean haqi = maodie && CatPartners.getMaodieHaqiTimer(entity) > 0;
		((CatStateCarrier) (Object) state).oldwu_setMaodie(maodie);
		((CatStateCarrier) (Object) state).oldwu_setMaodieHaqi(haqi);
		((CatStateCarrier) (Object) state).oldwu_setStateId(CatPartners.getState(entity).ordinal());
		((CatStateCarrier) (Object) state).oldwu_setDanceModelIndex(CatPartners.getDanceModelIndex(entity));
		((CatStateCarrier) (Object) state).oldwu_setMaodieAnimTick(CatPartners.getMaodieAnimTick(entity));
		((CatStateCarrier) (Object) state).oldwu_setHitgroundAnimTick(CatPartners.getHitgroundAnimTick(entity));
		// 狂暴状态（血量 ≤ 阈值）：用于常态循环播放翻滚动画
		((CatStateCarrier) (Object) state).oldwu_setMaodieRage(entity.getHealth() <= MaodieLogic.RAGE_THRESHOLD);
		if (maodie) {
			state.texture = haqi ? HAQI_TEXTURE : MAODIE_TEXTURE;
		}
		if (CatPartners.getState(entity) == CatState.RECOVERY) {
			// 回血状态绿色发光
			state.outlineColor = 0xFF00FF00;
		}
	}

	@Override
	public MaodieCatModel oldwu_getMaodieModel() {
		return this.oldwuMaodieModel;
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

	@Override
	public GroomingCatModel oldwu_getGroomingModel() {
		return this.oldwuGroomingModel;
	}

	@Override
	public GroomingCatBabyModel oldwu_getGroomingBabyModel() {
		return this.oldwuGroomingBabyModel;
	}

	@Override
	public HitGroundCatModel oldwu_getHitGroundModel() {
		return this.oldwuHitGroundModel;
	}

	@Override
	public HitGroundCatBabyModel oldwu_getHitGroundBabyModel() {
		return this.oldwuHitGroundBabyModel;
	}
}
