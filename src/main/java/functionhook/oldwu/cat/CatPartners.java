package functionhook.oldwu.cat;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.animal.feline.Cat;

public final class CatPartners {
	public static final EntityDataAccessor<String> PARTNER_UUID = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.STRING);
	public static final String NO_PARTNER = "";

	private CatPartners() {
	}

	public static Optional<UUID> getPartner(Cat cat) {
		String value = cat.getEntityData().get(PARTNER_UUID);
		if (value == null || value.isEmpty()) {
			return Optional.empty();
		}

		try {
			return Optional.of(UUID.fromString(value));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}

	public static void setPartner(Cat cat, UUID uuid) {
		cat.getEntityData().set(PARTNER_UUID, uuid == null ? NO_PARTNER : uuid.toString());
	}

	public static boolean isPaired(Cat cat) {
		return getPartner(cat).isPresent();
	}
}
