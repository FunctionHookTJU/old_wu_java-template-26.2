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
	public static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> ATTACK_COOLDOWN = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> FLAT_TIMER = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> PAIRING_TIMER = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> DANCE_MODEL_INDEX = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> DANCE_TIMER = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> MAODIE_HAQI_TIMER = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> MAODIE_RAGE_COOLDOWN = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> MAODIE_ANIM_TICK = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> MAODIE_NORMAL_FIRE_COOLDOWN = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> GROOMING_TIMER = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> BATTLE_PEACE_TIMER = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.INT);
	private CatPartners() {
	}

	public static int getDanceModelIndex(Cat cat) {
		return cat.getEntityData().get(DANCE_MODEL_INDEX);
	}

	public static void setDanceModelIndex(Cat cat, int value) {
		cat.getEntityData().set(DANCE_MODEL_INDEX, value);
	}

	public static int getDanceTimer(Cat cat) {
		return cat.getEntityData().get(DANCE_TIMER);
	}

	public static void setDanceTimer(Cat cat, int value) {
		cat.getEntityData().set(DANCE_TIMER, value);
	}

	/**
	 * 强制触发类初始化，确保所有 {@code EntityDataAccessor} 的 defineId
	 * 在 {@code SynchedEntityData.Builder} 构造前完成注册（Builder 的数组大小
	 * 在构造时按已注册数量固定，延迟注册会越界）。
	 */
	public static void initAccessors() {
		if (PARTNER_UUID == null || STATE == null || ATTACK_COOLDOWN == null || FLAT_TIMER == null || PAIRING_TIMER == null) {
			throw new IllegalStateException("Cat data accessors not initialized");
		}
	}

	public static int getMaodieHaqiTimer(Cat cat) {
		return cat.getEntityData().get(MAODIE_HAQI_TIMER);
	}

	public static void setMaodieHaqiTimer(Cat cat, int value) {
		cat.getEntityData().set(MAODIE_HAQI_TIMER, value);
	}

	public static int getMaodieRageCooldown(Cat cat) {
		return cat.getEntityData().get(MAODIE_RAGE_COOLDOWN);
	}

	public static void setMaodieRageCooldown(Cat cat, int value) {
		cat.getEntityData().set(MAODIE_RAGE_COOLDOWN, value);
	}

	public static int getMaodieAnimTick(Cat cat) {
		return cat.getEntityData().get(MAODIE_ANIM_TICK);
	}

	public static void setMaodieAnimTick(Cat cat, int value) {
		cat.getEntityData().set(MAODIE_ANIM_TICK, value);
	}

	public static int getMaodieNormalFireCooldown(Cat cat) {
		return cat.getEntityData().get(MAODIE_NORMAL_FIRE_COOLDOWN);
	}

	public static void setMaodieNormalFireCooldown(Cat cat, int value) {
		cat.getEntityData().set(MAODIE_NORMAL_FIRE_COOLDOWN, value);
	}

	public static int getPairingTimer(Cat cat) {
		return cat.getEntityData().get(PAIRING_TIMER);
	}

	public static void setPairingTimer(Cat cat, int value) {
		cat.getEntityData().set(PAIRING_TIMER, value);
	}

	public static int getFlatTimer(Cat cat) {
		return cat.getEntityData().get(FLAT_TIMER);
	}

	public static void setFlatTimer(Cat cat, int value) {
		cat.getEntityData().set(FLAT_TIMER, value);
	}

	public static int getGroomingTimer(Cat cat) {
		return cat.getEntityData().get(GROOMING_TIMER);
	}

	public static void setGroomingTimer(Cat cat, int value) {
		cat.getEntityData().set(GROOMING_TIMER, value);
	}

	public static int getAttackCooldown(Cat cat) {return cat.getEntityData().get(ATTACK_COOLDOWN);}

	public static void setAttackCooldown(Cat cat, int value) { cat.getEntityData().set(ATTACK_COOLDOWN, value);}

	public static int getBattlePeaceTimer(Cat cat) {return cat.getEntityData().get(BATTLE_PEACE_TIMER);}

	public static void setBattlePeaceTimer(Cat cat, int value) { cat.getEntityData().set(BATTLE_PEACE_TIMER, value);}

	public static CatState getState(Cat cat) {
		return CatState.fromInt(cat.getEntityData().get(STATE));
	}

	public static void setState(Cat cat, CatState state) {
		cat.getEntityData().set(STATE, state.ordinal());
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
