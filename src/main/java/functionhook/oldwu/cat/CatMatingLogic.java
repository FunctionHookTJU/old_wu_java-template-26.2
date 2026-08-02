package functionhook.oldwu.cat;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import functionhook.oldwu.audio.CatAudio;
import functionhook.oldwu.particle.ModParticles;

public final class CatMatingLogic {
	private static final double ATTRACT_RANGE = 16.0;
	private static final double STOP_DISTANCE_SQR = 2.25;
	private static final float BATTLE_TRIGGER_CHANCE = 0.05F;
	private static final int ATTACK_MIN_DELAY = 10;
	private static final int ATTACK_MAX_DELAY = 20;
	private static final float ATTACK_DAMAGE = 0.5F;
	private static final double GRAPPLE_DISTANCE_SQR = 0.5;
	private static final double GRAPPLE_TAIL_OFFSET = 0.55;
	private static final int PAIRING_DELAY_TICKS = 100;
	private static final double MINECART_RANGE = 16.0;
	private static final double MINECART_CONTACT_DISTANCE_SQR = 1.0;
	private static final int FLAT_DURATION_TICKS = 300;
	private static final float RECOVERY_HEALTH = 1.0F;
	private static final float RECOVERY_EXIT_RATIO = 0.8F;
	private static final int REGENERATION_DURATION = 200;

	private CatMatingLogic() {
	}

	public static void tick(ServerLevel level, Cat cat) {
		if (cat.isOrderedToSit()) {
			return;
		}

		if (CatPartners.getState(cat) == CatState.FLAT) {
			flatTick(cat);
			return;
		}

		if (trackMinecart(cat)) {
			return;
		}

		Optional<UUID> partnerId = CatPartners.getPartner(cat);
		if (partnerId.isPresent()) {
			LivingEntity partner = cat.level().getEntity(partnerId.get()) instanceof LivingEntity living ? living : null;
			if (partner == null) {
				CatPartners.setPartner(cat, null);
				transitionTo(cat, CatState.COMMON);
				return;
			}

			switch (CatPartners.getState(cat)) {
				case BATTLE -> battleTick(level, cat, partner);
				case RECOVERY -> recoveryTick(level, cat, partner);
				default -> pairingTick(cat, partner);
			}
		} else {
			tryAngryNearby(cat);
		}
	}

	/**
	 * 最高优先级：矿车追踪。16 格内存在矿车时立即中断当前状态并寻路至矿车，
	 * 接触矿车则进入压扁（flat）状态。
	 *
	 * @return 是否正在追踪矿车（该 tick 已由矿车逻辑接管）
	 */
	private static boolean trackMinecart(Cat cat) {
		AbstractMinecart minecart = findNearestMinecart(cat);
		if (minecart == null) {
			return false;
		}

		if (cat.distanceToSqr(minecart) <= MINECART_CONTACT_DISTANCE_SQR) {
			enterFlat(cat);
		} else {
			cat.getNavigation().moveTo(minecart, 1.0);
		}
		return true;
	}

	private static AbstractMinecart findNearestMinecart(Cat cat) {
		List<AbstractMinecart> carts = cat.level().getEntitiesOfClass(
			AbstractMinecart.class,
			new AABB(cat.blockPosition()).inflate(MINECART_RANGE),
			cart -> !cart.isRemoved()
		);
		if (carts.isEmpty()) {
			return null;
		}
		return carts.stream().min(Comparator.comparingDouble(cat::distanceToSqr)).orElse(null);
	}

	/**
	 * 进入压扁状态：无法移动，15 秒后恢复 common，并清除配对 UUID。
	 */
	public static void enterFlat(Cat cat) {
		CatPartners.setPartner(cat, null);
		CatPartners.setFlatTimer(cat, FLAT_DURATION_TICKS);
		transitionTo(cat, CatState.FLAT);
	}

	private static void flatTick(Cat cat) {
		cat.getNavigation().stop();
		Vec3 motion = cat.getDeltaMovement();
		cat.setDeltaMovement(motion.x * 0.2, Math.max(motion.y, 0.0), motion.z * 0.2);

		int timer = CatPartners.getFlatTimer(cat);
		if (timer > 0) {
			CatPartners.setFlatTimer(cat, timer - 1);
		} else {
			transitionTo(cat, CatState.COMMON);
		}
	}

	private static void tryAngryNearby(Cat cat) {
		findCandidate(cat).ifPresentOrElse(other -> {
			transitionTo(cat, CatState.ANGRY);
			if (cat.distanceToSqr(other) > STOP_DISTANCE_SQR) {
				cat.getNavigation().moveTo(other, 1.0);
			} else {
				cat.getNavigation().stop();
				CatPartners.setPartner(cat, other.getUUID());
				CatPartners.setPartner(other, cat.getUUID());
				CatPartners.setPairingTimer(cat, PAIRING_DELAY_TICKS);
				CatPartners.setPairingTimer(other, PAIRING_DELAY_TICKS);
				transitionTo(cat, CatState.PAIRING);
				transitionTo(other, CatState.PAIRING);
			}
		}, () -> {
			transitionTo(cat, CatState.COMMON);
			cat.getNavigation().stop();
		});
	}

	private static void pairingTick(Cat cat, LivingEntity partner) {
		transitionTo(cat, CatState.PAIRING);

		int timer = CatPartners.getPairingTimer(cat);
		if (timer > 0) {
			CatPartners.setPairingTimer(cat, timer - 1);
		}

		if (cat.distanceToSqr(partner) > STOP_DISTANCE_SQR) {
			cat.getNavigation().moveTo(partner, 1.0);
		} else {
			cat.getNavigation().stop();
			cat.getLookControl().setLookAt(partner, 30.0F, 30.0F);

			// 配对至少 5 秒后才可触发战斗
			if (timer <= 0 && cat.getRandom().nextFloat() < BATTLE_TRIGGER_CHANCE) {
				startBattle(cat, partner);
			}
		}
	}

	private static void startBattle(Cat cat, LivingEntity partner) {
		if (!(partner instanceof Cat other)) {
			return;
		}

		transitionTo(cat, CatState.BATTLE);
		transitionTo(other, CatState.BATTLE);
		CatPartners.setAttackCooldown(cat, nextAttackDelay(cat));
		CatPartners.setAttackCooldown(other, nextAttackDelay(other));
	}

	private static void battleTick(ServerLevel level, Cat cat, LivingEntity partner) {
		double distanceSqr = cat.distanceToSqr(partner);
		if (distanceSqr > STOP_DISTANCE_SQR) {
			cat.getNavigation().moveTo(partner, 1.0);
		} else {
			cat.getNavigation().stop();

			if (distanceSqr <= GRAPPLE_DISTANCE_SQR) {
				grapple(cat, partner);

				// 缠斗粒子：中低密度（每 3 tick 生成一个）
				if (cat.tickCount % 3 == 0) {
					spawnMaomaoParticles(level, cat);
				}
			}
		}

		int cooldown = CatPartners.getAttackCooldown(cat);
		if (cooldown > 0) {
			CatPartners.setAttackCooldown(cat, cooldown - 1);
		} else {
			// 互相攻击，但使用 generic（带 NO_KNOCKBACK 标签）伤害来源，击退为 0，避免缠斗被弹开。
			partner.hurtServer(level, cat.damageSources().generic(), ATTACK_DAMAGE);
			CatAudio.playStateSound(cat, CatState.BATTLE);
			CatPartners.setAttackCooldown(cat, nextAttackDelay(cat));
		}

		if (cat.getHealth() <= RECOVERY_HEALTH || partner.getHealth() <= RECOVERY_HEALTH) {
			startRecovery(cat, partner);
		}
	}

	/**
	 * 缠斗姿态：两只猫紧贴、头尾相对（a 的头在 b 的脚部，b 的头在 a 的脚部）。
	 * 每只猫都朝向对方身体的后半段，形成互相咬尾的纠缠效果。
	 */
	private static void grapple(Cat cat, LivingEntity partner) {
		Vec3 tail = partner.position().subtract(partner.getLookAngle().scale(GRAPPLE_TAIL_OFFSET));
		float yaw = (float) (Mth.atan2(tail.z - cat.getZ(), tail.x - cat.getX()) * (180.0 / Math.PI));

		cat.setYRot(yaw);
		cat.yBodyRot = yaw;
		cat.yHeadRot = yaw;
		cat.getLookControl().setLookAt(tail);
	}

	private static void startRecovery(Cat cat, LivingEntity partner) {
		if (!(partner instanceof Cat other)) {
			return;
		}

		transitionTo(cat, CatState.RECOVERY);
		transitionTo(other, CatState.RECOVERY);
		applyRecoveryEffects(cat);
		applyRecoveryEffects(other);
	}

	private static void recoveryTick(ServerLevel level, Cat cat, LivingEntity partner) {
		if (!(partner instanceof Cat other)) {
			CatPartners.setPartner(cat, null);
			transitionTo(cat, CatState.COMMON);
			return;
		}

		transitionTo(cat, CatState.RECOVERY);
		applyRecoveryEffects(cat);
		spawnRecoveryParticles(level, cat);
		spawnRecoveryParticles(level, other);

		float maxHealth = cat.getMaxHealth();
		if (cat.getHealth() > maxHealth * RECOVERY_EXIT_RATIO && other.getHealth() > other.getMaxHealth() * RECOVERY_EXIT_RATIO) {
			CatPartners.setPartner(cat, null);
			CatPartners.setPartner(other, null);
			transitionTo(cat, CatState.COMMON);
			transitionTo(other, CatState.COMMON);
		}
	}

	private static void spawnMaomaoParticles(ServerLevel level, Cat cat) {
		level.sendParticles(
			ModParticles.MAOMAO,
			cat.getX(),
			cat.getY() + cat.getBbHeight() * 0.5,
			cat.getZ(),
			1,
			cat.getBbWidth() * 0.5,
			cat.getBbHeight() * 0.4,
			cat.getBbWidth() * 0.5,
			0.02
		);
	}

	private static void spawnRecoveryParticles(ServerLevel level, Cat cat) {
		level.sendParticles(
			ModParticles.RECOVERY,
			cat.getX(),
			cat.getY() + cat.getBbHeight() * 0.5,
			cat.getZ(),
			1,
			cat.getBbWidth() * 0.4,
			cat.getBbHeight() * 0.3,
			cat.getBbWidth() * 0.4,
			0.02
		);
	}

	/**
	 * 回血效果：生命恢复 I（无粒子）+ 缓慢 III（无粒子），
	 * 只保留模组的 recovery 粒子效果。
	 */
	private static void applyRecoveryEffects(Cat cat) {
		if (!cat.hasEffect(MobEffects.REGENERATION)) {
			cat.addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGENERATION_DURATION, 0, false, false, false));
		}
		if (!cat.hasEffect(MobEffects.SLOWNESS)) {
			cat.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, REGENERATION_DURATION, 2, false, false, false));
		}
	}

	private static int nextAttackDelay(Cat cat) {
		return ATTACK_MIN_DELAY + cat.getRandom().nextInt(ATTACK_MAX_DELAY - ATTACK_MIN_DELAY + 1);
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

	private static void transitionTo(Cat cat, CatState newState) {
		if (CatPartners.getState(cat) != newState) {
			CatPartners.setState(cat, newState);
			CatAudio.playStateSound(cat, newState);
		}
	}
}
