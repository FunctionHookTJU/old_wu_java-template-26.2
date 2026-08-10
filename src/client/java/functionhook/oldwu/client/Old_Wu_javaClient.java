package functionhook.oldwu.client;

import com.ibm.icu.text.Normalizer2;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import functionhook.oldwu.Old_Wu_java;
import functionhook.oldwu.client.model.AngryCatBabyModel;
import functionhook.oldwu.client.model.AngryCatModel;
import functionhook.oldwu.client.model.BattleCatBabyModel;
import functionhook.oldwu.client.model.BattleCatModel;
import functionhook.oldwu.client.model.FlatCatBabyModel;
import functionhook.oldwu.client.model.FlatCatModel;
import functionhook.oldwu.client.model.HitGroundCatBabyModel;
import functionhook.oldwu.client.model.HitGroundCatModel;
import functionhook.oldwu.client.model.MaodieCatModel;
import functionhook.oldwu.client.model.PaperRollModel;
import functionhook.oldwu.client.model.RecoveryCatBabyModel;
import functionhook.oldwu.client.model.RecoveryCatModel;
import functionhook.oldwu.client.particle.MaomaoParticle;
import functionhook.oldwu.client.particle.RecoveryParticle;
import functionhook.oldwu.client.render.PaperRollRenderer;
import functionhook.oldwu.client.render.CatInfoHud;
import functionhook.oldwu.client.render.WolfFeedHud;
import functionhook.oldwu.entity.ModEntityTypes;
import functionhook.oldwu.particle.ModParticles;
import functionhook.oldwu.client.model.GroomingCatModel;

public class Old_Wu_javaClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ModelLayerRegistry.registerModelLayer(MaodieCatModel.LAYER_LOCATION, MaodieCatModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(AngryCatModel.LAYER_LOCATION, AngryCatModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(AngryCatBabyModel.LAYER_LOCATION, AngryCatBabyModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(BattleCatModel.LAYER_LOCATION, BattleCatModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(BattleCatBabyModel.LAYER_LOCATION, BattleCatBabyModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(RecoveryCatModel.LAYER_LOCATION, RecoveryCatModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(RecoveryCatBabyModel.LAYER_LOCATION, RecoveryCatBabyModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(FlatCatModel.LAYER_LOCATION, FlatCatModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(FlatCatBabyModel.LAYER_LOCATION, FlatCatBabyModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(PaperRollModel.LAYER_LOCATION, PaperRollModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(GroomingCatModel.LAYER_LOCATION, GroomingCatModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(HitGroundCatModel.LAYER_LOCATION, HitGroundCatModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(HitGroundCatBabyModel.LAYER_LOCATION, HitGroundCatBabyModel::createBodyLayer);

		EntityRendererRegistry.register(ModEntityTypes.PAPER_ROLL, PaperRollRenderer::new);

		ParticleProviderRegistry.getInstance().register(ModParticles.RECOVERY, RecoveryParticle.Provider::new);
		ParticleProviderRegistry.getInstance().register(ModParticles.MAOMAO, MaomaoParticle.Provider::new);

		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Old_Wu_java.id("wolf_feed_progress"), WolfFeedHud::extract);
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Old_Wu_java.id("cat_info"), CatInfoHud::extract);
	}
}
