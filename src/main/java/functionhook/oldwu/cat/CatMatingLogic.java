package functionhook.oldwu.cat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.phys.AABB;

public final class CatMatingLogic {
	private static final double ATTRACT_RANGE = 8.0;
	private static final double STOP_DISTANCE_SQR = 2.25;

	private CatMatingLogic() {
	}

	public static void tick(Cat cat) {
		if (cat.isOrderedToSit()) {
			return;
		}

		Optional<UUID> partnerId = CatPartners.getPartner(cat);
		if (partnerId.isPresent()) {
			moveTowardsPartner(cat, partnerId.get());
		} else {
			tryPairWithNearby(cat);
		}
	}

	private static void tryPairWithNearby(Cat cat) {
		findCandidate(cat).ifPresent(other -> {
			CatPartners.setPartner(cat, other.getUUID());
			CatPartners.setPartner(other, cat.getUUID());
		});
	}

	private static Optional<Cat> findCandidate(Cat cat) {
		List<Cat> candidates = cat.level().getEntitiesOfClass(
			Cat.class,
			new AABB(cat.blockPosition()).inflate(ATTRACT_RANGE),
			candidate -> candidate != cat && !CatPartners.isPaired(candidate) && !candidate.isOrderedToSit()
		);
		if (candidates.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(candidates.get(cat.getRandom().nextInt(candidates.size())));
	}

	private static void moveTowardsPartner(Cat cat, UUID partnerId) {
		LivingEntity partner = cat.level().getEntity(partnerId) instanceof LivingEntity living ? living : null;
		if (partner == null) {
			CatPartners.setPartner(cat, null);
			return;
		}

		if (cat.distanceToSqr(partner) > STOP_DISTANCE_SQR) {
			cat.getNavigation().moveTo(partner, 1.0);
		} else {
			cat.getNavigation().stop();
		}
	}
}
