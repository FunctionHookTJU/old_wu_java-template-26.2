package functionhook.oldwu.advancement;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import functionhook.oldwu.cat.CatMatingLogic;

/** Tracks two consecutive telescope observations of the cat named maodie. */
public final class MaodieObservationTracker {
	private static final Logger LOGGER = LoggerFactory.getLogger("old_wu_java/maodie_observation");
	private static final Identifier ADVANCEMENT_ID = Identifier.fromNamespaceAndPath("old_wu_java", "spot_check_camera");
	private static final double OBSERVATION_RANGE = 64.0;
	private static final Map<UUID, ObservationState> STATES = new HashMap<>();

	private MaodieObservationTracker() {
	}

	public static void initialize() {
		ServerTickEvents.END_SERVER_TICK.register(MaodieObservationTracker::tick);
		LOGGER.info("Maodie observation tracker initialized");
	}

	private static void tick(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			ObservationState state = STATES.computeIfAbsent(player.getUUID(), ignored -> new ObservationState());
			boolean observing = isObservingMaodie(player);
			if (!observing) {
				state.observing = false;
				continue;
			}

			if (!state.observing) {
				state.observations++;
				if (state.observations >= 2) {
					AdvancementHolder advancement = server.getAdvancements().get(ADVANCEMENT_ID);
					if (advancement != null) {
						if (player.getAdvancements().award(advancement, "observe_maodie")) {
							LOGGER.info("Awarded {} to {}", ADVANCEMENT_ID, player.getName().getString());
						}
					} else {
						LOGGER.warn("Advancement {} was not loaded", ADVANCEMENT_ID);
					}
					state.observations = 0;
				}
			}

			state.observing = true;
		}
	}

	private static boolean isObservingMaodie(ServerPlayer player) {
		ItemStack useItem = player.getUseItem();
		if (!player.isUsingItem() || !useItem.is(Items.SPYGLASS)) {
			return false;
		}

		Vec3 start = player.getEyePosition();
		Vec3 view = player.getViewVector(1.0F).normalize();
		Cat target = null;
		double nearest = Double.MAX_VALUE;
		for (Cat cat : player.level().getEntitiesOfClass(Cat.class, player.getBoundingBox().inflate(OBSERVATION_RANGE))) {
			if (!CatMatingLogic.isMaodie(cat) || !player.hasLineOfSight(cat)) {
				continue;
			}
			double distance = cat.distanceToSqr(start);
			if (distance > OBSERVATION_RANGE * OBSERVATION_RANGE) {
				continue;
			}
			Vec3 toCat = cat.getBoundingBox().getCenter().subtract(start).normalize();
			// 用视线夹角而不是精确射线穿过碰撞箱，避免放大的耄耋模型与原始碰撞箱不一致。
			if (view.dot(toCat) < 0.985) {
				continue;
			}
			if (distance < nearest) {
				nearest = distance;
				target = cat;
			}
		}
		return target != null;
	}

	private static final class ObservationState {
		private int observations;
		private boolean observing;
	}
}
