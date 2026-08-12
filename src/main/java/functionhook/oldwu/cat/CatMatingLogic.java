package functionhook.oldwu.cat;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import functionhook.oldwu.audio.CatAudio;
import functionhook.oldwu.block.ModBlocks;
import functionhook.oldwu.particle.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class CatMatingLogic {
	private static final double ATTRACT_RANGE = 16.0;//两只猫互相攻击的检测范围
	private static final double STOP_DISTANCE_SQR = 2.25;
	private static final float BATTLE_TRIGGER_CHANCE = 0.05F;
	private static final float BATTLE_JUMP_CHANCE = 0.02F;
	private static final int ATTACK_MIN_DELAY = 10;
	private static final int ATTACK_MAX_DELAY = 20;
	private static final float ATTACK_DAMAGE = 0.5F;
	private static final double GRAPPLE_DISTANCE_SQR = 0.5;
	private static final double GRAPPLE_TAIL_OFFSET = 0.55;
	private static final int PAIRING_DELAY_TICKS = 100;
	private static final double MINECART_RANGE = 16.0;
	private static final double MINECART_CONTACT_DISTANCE_SQR = 1.0;
	private static final int FLAT_DURATION_TICKS = 300;
	private static final double DANCE_RANGE = 0.5;    //进入街舞状态检测范围
	private static final int DANCE_DURATION_TICKS = 100;
	private static final int DANCE_MODEL_SWITCH_INTERVAL = 5;
	private static final int DANCE_MODEL_COUNT = 5;
	private static final float RECOVERY_HEALTH = 1.0F;
	private static final float RECOVERY_EXIT_RATIO = 0.8F;
	private static final int REGENERATION_DURATION = 200;
	public static final String MAODIE_NAME = "maodie";
	/** 镜子触发愤怒的检测范围（格）。 */
	private static final double MIRROR_RANGE = 3.0;
	/** 老吴撼地掌：玩家索敌范围（格）。 */
	private static final double PLAYER_RANGE = 16.0;
	/** 老吴撼地掌：近战攻击距离平方（3 格）。 */
	private static final double PALM_ATTACK_RANGE_SQR = 9.0;
	/** 老吴撼地掌：动画时长（tick，对应 0.5s 动画）。 */
	private static final int PALM_DURATION_TICKS = 10;
	/** 老吴撼地掌：攻击间隔（tick）。 */
	private static final int PALM_COOLDOWN_TICKS = 10;
	/** 普通猫被玩家攻击后的反击窗口（tick）。 */
	private static final int RETALIATION_WINDOW_TICKS = 200;

	private CatMatingLogic() {
	}

	/**
	 * 是否被命名牌命名为 "maodie"：此类猫使用专用模型/贴图，
	 * 移除全部 AI 与行为逻辑，且不发出任何声音。
	 */
	public static boolean isMaodie(Cat cat) {
		return cat.hasCustomName() && MAODIE_NAME.equals(cat.getCustomName().getString());
	}

	/**
	 * 曾经被命名为 "maodie" 的猫（无论驯服与否）：改名后保留 325 血量上限，
	 * 不再应用键帽/已驯服的血量加成。
	 */
	private static void ensureRetainedMaodieHealth(Cat cat) {
		var attribute = cat.getAttribute(Attributes.MAX_HEALTH);
		if (attribute != null && attribute.getBaseValue() != MaodieLogic.MAX_HEALTH) {
			attribute.setBaseValue(MaodieLogic.MAX_HEALTH);
			cat.setHealth(cat.getMaxHealth());
		}
	}

	public static void tick(ServerLevel level, Cat cat) {
		// 好猫值懒分配（覆盖所有猫；在任何分支前完成，保证驯服判定等逻辑可用）
		GoodCatLogic.assignRandomIfAbsent(cat);
		// 曾是 maodie 则记录标记；已驯服/键帽加成由 maodie 自身管理血量（325），
		// 普通猫仅在从未当过 maodie 时应用加成，避免覆盖已保留的 325 血量。
		if (isMaodie(cat)) {
			CatPartners.setWasMaodie(cat, true);
		} else if (CatPartners.getWasMaodie(cat)) {
			ensureRetainedMaodieHealth(cat);
		} else {
			GoodCatLogic.applyKeycapBonuses(cat);
			GoodCatLogic.applyTamedBonuses(cat);
		}

		int peaceTimer = CatPartners.getBattlePeaceTimer(cat);
		if (peaceTimer > 0) {
			CatPartners.setBattlePeaceTimer(cat, peaceTimer - 1);
			if (peaceTimer % 5 == 0) {
				spawnWetParticles(level, cat);
			}
		}

		// 撼地掌攻击间隔冷却全局递减
		int hitCooldown = CatPartners.getHitgroundCooldown(cat);
		if (hitCooldown > 0) {
			CatPartners.setHitgroundCooldown(cat, hitCooldown - 1);
		}

		if (!isMaodie(cat)) {
			// 不再是被命名为 "maodie" 的猫时，清理残留的 Boss 血条，并恢复被暂存的原版 AI 目标
			MaodieLogic.removeBossBar(cat);
			MaodieLogic.restoreAiIfNeeded(cat);
		}

		if (cat.isOrderedToSit()) {
			return;
		}

		if (isMaodie(cat)) {
			// maodie 的专属行为逻辑（攻击玩家、狂暴发射纸筒等）
			CatPartners.setPartner(cat, null);
			MaodieLogic.tick(level, cat);
			return;
		}

		if (CatPartners.getState(cat) != CatState.RECOVERY && cat.hasGlowingTag()) {
			cat.setGlowingTag(false);
		}

		if (CatPartners.getState(cat) == CatState.FLAT) {
			flatTick(cat);
			return;
		}

		if (CatPartners.getState(cat) == CatState.DANCE) {
			danceTick(cat);
			return;
		}

		if (CatPartners.getState(cat) == CatState.GROOMING) {
			groomingTick(level, cat);
			return;
		}

		if (CatPartners.getState(cat) == CatState.HITGROUND) {
			hitgroundTick(level, cat);
			return;
		}

		// 无法进入生气或配对模式，这段是对应代码
		if (CatPartners.getBattlePeaceTimer(cat) > 0) {
			CatPartners.getPartner(cat).ifPresent(partnerId -> {
				if (cat.level().getEntity(partnerId) instanceof Cat other) {
					CatPartners.setPartner(other, null);

					if (CatPartners.getState(other) == CatState.ANGRY
							|| CatPartners.getState(other) == CatState.PAIRING
							|| CatPartners.getState(other) == CatState.BATTLE) {
						transitionTo(other, CatState.COMMON);
					}
				}
			});

			CatPartners.setPartner(cat, null);

			if (CatPartners.getState(cat) == CatState.ANGRY
					|| CatPartners.getState(cat) == CatState.PAIRING
					|| CatPartners.getState(cat) == CatState.BATTLE) {
				transitionTo(cat, CatState.COMMON);
			}

			return;
		}

		// dance/flat 可打断任意状态：0.5 格内被马/驴/骡/猪/骆驼冲撞
		if (tryDanceOrFlat(cat)) {
			return;
		}

		if (trackMinecart(cat)) {
			return;
		}

		// 猫能观察到 3 格内的镜子时进入 angry（maodie 由上面的分支直接返回，不会触发）
		if (tryMirrorAngry(cat)) {
			return;
		}

		// 回血：战斗中任意一方达到条件则双方同时回血（回完继续战斗）；其余情况单猫回血
		if (CatPartners.getState(cat) == CatState.RECOVERY) {
			if (!recoveryTickPair(level, cat)) {
				recoveryTick(level, cat);
			}
			return;
		}

		if (cat.getHealth() <= RECOVERY_HEALTH && !(CatPartners.getState(cat) == CatState.BATTLE && CatPartners.isPaired(cat))) {
			startRecovery(cat);
			return;
		}

		Optional<UUID> partnerId = CatPartners.getPartner(cat);
		if (partnerId.isPresent()) {
			// 范围内出现 maodie 时，普通猫停止互相配对，切换目标到 maodie
			if (retargetToMaodie(cat, partnerId.get())) {
				return;
			}

			LivingEntity partner = cat.level().getEntity(partnerId.get()) instanceof LivingEntity living ? living : null;
			if (partner == null) {
				CatPartners.setPartner(cat, null);
				transitionTo(cat, CatState.COMMON);
				return;
			}

			switch (CatPartners.getState(cat)) {
				case BATTLE -> battleTick(level, cat, partner);
				default -> pairingTick(cat, partner);
			}
		} else {
			tryAngryNearby(level, cat);
		}
	}

	/**
	 * 被马/驴/骡/猪/骆驼（无论是否被骑乘）冲撞或经过时，猫 50% 概率进入 dance、50% 概率进入 flat。
	 */
	private static boolean tryDanceOrFlat(Cat cat) {
		if (findNearbyMount(cat) == null) {
			return false;
		}

		if (cat.getRandom().nextBoolean()) {
			enterDance(cat);
		} else {
			enterFlat(cat);
		}
		return true;
	}

	private static Entity findNearbyMount(Cat cat) {
		AABB range = new AABB(cat.blockPosition()).inflate(DANCE_RANGE);
		List<AbstractHorse> horses = cat.level().getEntitiesOfClass(AbstractHorse.class, range, horse -> !horse.isRemoved());
		if (!horses.isEmpty()) {
			return horses.get(0);
		}
		List<Camel> camels = cat.level().getEntitiesOfClass(Camel.class, range, camel -> !camel.isRemoved());
		if (!camels.isEmpty()) {
			return camels.get(0);
		}
		List<Pig> pigs = cat.level().getEntitiesOfClass(Pig.class, range, pig -> !pig.isRemoved());
		return pigs.isEmpty() ? null : pigs.get(0);
	}

	/**
	 * 进入 dance 状态：不停跳跃，模型在全部状态模型间每 5 tick 随机切换，
	 * 不播放任何音效；持续 {@link #DANCE_DURATION_TICKS} 后恢复 common。
	 */
	public static void enterDance(Cat cat) {
		CatPartners.setPartner(cat, null);
		CatPartners.setDanceTimer(cat, DANCE_DURATION_TICKS);
		CatPartners.setDanceModelIndex(cat, 0);
		transitionTo(cat, CatState.DANCE);
	}

	private static void danceTick(Cat cat) {
		if (cat.onGround()) {
			cat.jumpFromGround();
		}

		if (cat.tickCount % DANCE_MODEL_SWITCH_INTERVAL == 0) {
			CatPartners.setDanceModelIndex(cat, cat.getRandom().nextInt(DANCE_MODEL_COUNT));
		}

		int timer = CatPartners.getDanceTimer(cat);
		if (timer > 0) {
			CatPartners.setDanceTimer(cat, timer - 1);
		} else {
			transitionTo(cat, CatState.COMMON);
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

	private static void tryAngryNearby(ServerLevel level, Cat cat) {
		findCandidate(cat).ifPresentOrElse(
			other -> chaseAndPair(cat, other),
			() -> {
				// 无配对候选时，未驯服猫攻击玩家、已驯服猫攻击敌对/指定生物（配对优先级高于攻击）
				if (!tryAttackTarget(level, cat)) {
					if (transitionTo(cat, CatState.COMMON)) {
						cat.getNavigation().stop();
					}
				}
			}
		);
	}

	/**
	 * 猫能观察到 3 格内的镜子时进入 pairing 状态（原地、视角锁定在镜子上不转身）。
	 *
	 * <p>仅普通猫触发（maodie 在 tick 早期分支已返回）；镜子不可见或超出范围后恢复 common。
	 *
	 * <p>优先级：<b>低于与其他猫的配对</b>——若存在可配对的其它猫（或 maodie），
	 * 即使当前正对镜注视，也优先让出控制、转而去与其他猫配对。
	 *
	 * @return 本 tick 是否已由"对镜注视"逻辑接管
	 */
	private static boolean tryMirrorAngry(Cat cat) {
		// 对镜配对的优先级低于对其他猫的配对：存在可配对目标时让出控制
		if (findCandidate(cat).isPresent()) {
			if (CatPartners.getMirrorTicks(cat) > 0) {
				CatPartners.setMirrorTicks(cat, 0);
				CatPartners.setPartner(cat, null);
				transitionTo(cat, CatState.COMMON);
				cat.getNavigation().stop();
			}
			return false;
		}

		BlockPos mirror = findVisibleMirror(cat);
		if (mirror == null) {
			// 若正处于对镜注视且镜子已不可见，恢复 common
			if (CatPartners.getMirrorTicks(cat) > 0) {
				CatPartners.setMirrorTicks(cat, 0);
				CatPartners.setPartner(cat, null);
				transitionTo(cat, CatState.COMMON);
				cat.getNavigation().stop();
			}
			return false;
		}

		CatPartners.setPartner(cat, null);
		CatPartners.setMirrorTicks(cat, 1);
		transitionTo(cat, CatState.PAIRING);

		// 视角锁定在镜子中心，不移动、不转身
		Vec3 mirrorCenter = Vec3.atCenterOf(mirror);
		cat.getLookControl().setLookAt(mirrorCenter.x, mirrorCenter.y, mirrorCenter.z, 360.0F, 360.0F);
		cat.getNavigation().stop();

		// 进入配对状态时播放配对（laowu 系列）音效；playStateSound 自带播放间隔，不会重叠
		CatAudio.playStateSound(cat, CatState.PAIRING);
		return true;
	}

	/**
	 * 在 3 格范围内查找"猫能观察到"的镜子方块（以猫的眼睛为起点、镜子中心为终点做射线检测）。
	 */
	private static BlockPos findVisibleMirror(Cat cat) {
		Vec3 eye = cat.getEyePosition();
		int range = (int) Math.ceil(MIRROR_RANGE);
		for (BlockPos pos : BlockPos.betweenClosed(cat.blockPosition().offset(-range, -range, -range), cat.blockPosition().offset(range, range, range))) {
			if (!cat.level().getBlockState(pos).is(ModBlocks.MIRROR)) {
				continue;
			}
			if (cat.distanceToSqr(Vec3.atCenterOf(pos)) > MIRROR_RANGE * MIRROR_RANGE) {
				continue;
			}
			Vec3 mirrorCenter = Vec3.atCenterOf(pos);
			BlockHitResult hit = cat.level().clip(new ClipContext(
				eye,
				mirrorCenter,
				ClipContext.Block.VISUAL,
				ClipContext.Fluid.NONE,
				cat
			));
			// 射线未被阻挡（直达镜子），或命中点就是镜子本身
			if (hit.getType() == HitResult.Type.MISS
					|| cat.level().getBlockState(hit.getBlockPos()).is(ModBlocks.MIRROR)) {
				return pos;
			}
		}
		return null;
	}

	/**
	 * 走向目标并进入 angry；到达附近后互存 UUID、进入 pairing。
	 */
	private static void chaseAndPair(Cat cat, Cat target) {
		transitionTo(cat, CatState.ANGRY);
		if (cat.distanceToSqr(target) > STOP_DISTANCE_SQR) {
			cat.getNavigation().moveTo(target, 1.0);
		} else {
			cat.getNavigation().stop();
			CatPartners.setPartner(cat, target.getUUID());
			CatPartners.setPartner(target, cat.getUUID());
			CatPartners.setPairingTimer(cat, PAIRING_DELAY_TICKS);
			CatPartners.setPairingTimer(target, PAIRING_DELAY_TICKS);
			transitionTo(cat, CatState.PAIRING);
			transitionTo(target, CatState.PAIRING);
		}
	}

	/**
	 * 普通猫已与另一只普通猫配对时，若索敌范围内出现 maodie，则解除原配对并转向 maodie。
	 *
	 * @return 本 tick 是否已由 maodie 目标接管
	 */
	private static boolean retargetToMaodie(Cat cat, UUID currentPartnerId) {
		Cat maodie = findNearbyMaodie(cat);
		if (maodie == null || maodie.getUUID().equals(currentPartnerId)) {
			return false;
		}

		// 解除与原普通猫的配对，使其也能重新索敌到 maodie
		if (cat.level().getEntity(currentPartnerId) instanceof Cat other) {
			CatPartners.setPartner(other, null);
			transitionTo(other, CatState.COMMON);
		}
		CatPartners.setPartner(cat, null);
		chaseAndPair(cat, maodie);
		return true;
	}

	private static Optional<Cat> findCandidate(Cat cat) {
		// 索敌范围内存在 maodie 时，普通猫停止互相配对，优先以 maodie 为目标
		Cat maodie = findNearbyMaodie(cat);
		if (maodie != null) {
			return Optional.of(maodie);
		}

		// 驯服的猫不会主动进入配对/战斗状态（含驯服猫互相之间）；
		// 只有未驯服的猫会挑衅发起配对/战斗
		if (cat.isTame()) {
			return Optional.empty();
		}

		List<Cat> candidates = cat.level().getEntitiesOfClass(
			Cat.class,
			new AABB(cat.blockPosition()).inflate(ATTRACT_RANGE),
			candidate -> candidate != cat
				&& !CatPartners.isPaired(candidate)
				&& !candidate.isOrderedToSit()
				// 绝世好猫不会主动与其他绝世好猫配对/战斗
				&& !(GoodCatLogic.isPeerless(cat) && GoodCatLogic.isPeerless(candidate))
		);
		if (candidates.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(candidates.get(cat.getRandom().nextInt(candidates.size())));
	}

	/**
	 * 索敌范围内是否存在被命名为 "maodie" 的猫。
	 */
	private static Cat findNearbyMaodie(Cat cat) {
		List<Cat> maodies = cat.level().getEntitiesOfClass(
			Cat.class,
			new AABB(cat.blockPosition()).inflate(ATTRACT_RANGE),
			candidate -> candidate != cat && isMaodie(candidate) && !candidate.isRemoved()
		);
		return maodies.isEmpty() ? null : maodies.get(cat.getRandom().nextInt(maodies.size()));
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
		if (CatPartners.getBattlePeaceTimer(cat) > 0
				|| CatPartners.getBattlePeaceTimer(other) > 0) {
			CatPartners.setPartner(cat, null);
			CatPartners.setPartner(other, null);
			transitionTo(cat, CatState.COMMON);
			transitionTo(other, CatState.COMMON);
			return;
		}
		transitionTo(cat, CatState.BATTLE);
		transitionTo(other, CatState.BATTLE);
		CatPartners.setAttackCooldown(cat, nextAttackDelay(cat));
		CatPartners.setAttackCooldown(other, nextAttackDelay(other));
	}

	private static void battleTick(ServerLevel level, Cat cat, LivingEntity partner) {
		double distanceSqr = cat.distanceToSqr(partner);

		// 每 tick 2% 概率跳起
		if (cat.getRandom().nextFloat() < BATTLE_JUMP_CHANCE) {
			cat.jumpFromGround();
		}

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
		} else if (distanceSqr <= STOP_DISTANCE_SQR) {
			// 仅在贴近范围内攻击，避免无视距离出手；
			// 使用 generic（带 NO_KNOCKBACK 标签）伤害来源，击退为 0，避免缠斗被弹开。
			partner.hurtServer(level, cat.damageSources().generic(), ATTACK_DAMAGE);
			CatAudio.playStateSound(cat, CatState.BATTLE);
			CatPartners.setAttackCooldown(cat, nextAttackDelay(cat));
		}

		// 战斗中任意一方生命 ≤1，双方同时进入回血（回血结束后继续战斗）；
		// 若对手是 maodie 则由其自行管理，普通猫单独回血
		if (cat.getHealth() <= RECOVERY_HEALTH || partner.getHealth() <= RECOVERY_HEALTH) {
			if (partner instanceof Cat other && !isMaodie(other)) {
				startRecoveryPair(cat, other);
			} else {
				startRecovery(cat);
			}
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

	/**
	 * 任意情况下生命 ≤1 进入回血：清除配对、施加恢复效果、开启绿色发光。
	 */
	private static void startRecovery(Cat cat) {
		CatPartners.setPartner(cat, null);
		transitionTo(cat, CatState.RECOVERY);
		applyRecoveryEffects(cat);
		cat.setGlowingTag(true);
	}

	private static void recoveryTick(ServerLevel level, Cat cat) {
		transitionTo(cat, CatState.RECOVERY);
		applyRecoveryEffects(cat);
		spawnRecoveryParticles(level, cat);

		float maxHealth = cat.getMaxHealth();
		if (cat.getHealth() > maxHealth * RECOVERY_EXIT_RATIO) {
			cat.setGlowingTag(false);
			transitionTo(cat, CatState.COMMON);
		}
	}

	/**
	 * 战斗回血：两只猫同时进入回血（保留配对、开启发光），
	 * 双方都恢复到 80% 以上后回到战斗。
	 */
	private static void startRecoveryPair(Cat cat, Cat other) {
		transitionTo(cat, CatState.RECOVERY);
		transitionTo(other, CatState.RECOVERY);
		applyRecoveryEffects(cat);
		applyRecoveryEffects(other);
		cat.setGlowingTag(true);
		other.setGlowingTag(true);
	}

	/**
	 * @return 是否作为配对双猫回血处理
	 */
	private static boolean recoveryTickPair(ServerLevel level, Cat cat) {
		Optional<UUID> partnerId = CatPartners.getPartner(cat);
		if (partnerId.isEmpty()) {
			return false;
		}
		if (!(cat.level().getEntity(partnerId.get()) instanceof Cat other)) {
			CatPartners.setPartner(cat, null);
			return false;
		}
		if (CatPartners.getState(other) != CatState.RECOVERY) {
			return false;
		}

		recoveryTickPairBoth(level, cat, other);
		return true;
	}

	private static void recoveryTickPairBoth(ServerLevel level, Cat cat, Cat other) {
		transitionTo(cat, CatState.RECOVERY);
		transitionTo(other, CatState.RECOVERY);
		applyRecoveryEffects(cat);
		applyRecoveryEffects(other);
		spawnRecoveryParticles(level, cat);
		spawnRecoveryParticles(level, other);

		float maxCat = cat.getMaxHealth();
		float maxOther = other.getMaxHealth();
		if (cat.getHealth() > maxCat * RECOVERY_EXIT_RATIO && other.getHealth() > other.getMaxHealth() * RECOVERY_EXIT_RATIO) {
			cat.setGlowingTag(false);
			other.setGlowingTag(false);
			transitionTo(cat, CatState.BATTLE);
			transitionTo(other, CatState.BATTLE);
			CatPartners.setAttackCooldown(cat, nextAttackDelay(cat));
			CatPartners.setAttackCooldown(other, nextAttackDelay(other));
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

	/**
	 * 状态切换；仅在状态确实变化时触发音频并返回 true。
	 */
	private static boolean transitionTo(Cat cat, CatState newState) {
		if (CatPartners.getState(cat) != newState) {
			CatPartners.setState(cat, newState);
			CatAudio.playStateSound(cat, newState);
			return true;
		}
		return false;
	}

	private static void groomingTick(ServerLevel level, Cat cat){
		cat.getNavigation().stop();

		int timer = CatPartners.getGroomingTimer(cat);
		if (timer > 0){
			CatPartners.setGroomingTimer(cat, timer - 1);
		} else {
			transitionTo(cat, CatState.COMMON);
		}
	}

	private static void spawnWetParticles(ServerLevel level, Cat cat) {
		level.sendParticles(
			ParticleTypes.FALLING_WATER,
			cat.getX(),
			cat.getY() + cat.getBbHeight() * 0.75,
			cat.getZ(),
			10,
			cat.getBbWidth() * 0.4,
			cat.getBbHeight() * 0.25,
			cat.getBbWidth() * 0.4,
			0.02
		);
	}

	/**
	 * 老吴撼地掌总入口（仅无配对候选时调用，配对优先级高于攻击）：
	 * <ul>
	 *   <li>未驯服：键帽/坏猫主动攻击玩家，普通猫被玩家攻击后反击；绝世好猫永不攻击。</li>
	 *   <li>已驯服：普通猫/绝世好猫主动攻击玩家以外的目标（敌对生物/玩家指定生物），
	 *       恐吓 10% / 攻击 90%。</li>
	 * </ul>
	 *
	 * @return 本 tick 是否已由攻击逻辑接管
	 */
	private static boolean tryAttackTarget(ServerLevel level, Cat cat) {
		if (cat.isTame()) {
			return tryTamedPalmAttack(level, cat);
		}
		if (GoodCatLogic.isPeerless(cat)) {
			// 未驯服绝世好猫任意情况不攻击
			return false;
		}
		return tryPlayerPalmAttack(level, cat);
	}

	/**
	 * 未驯服猫对玩家的撼地掌：坏猫/键帽主动攻击玩家，普通猫在被玩家攻击后一段时间内反击。
	 */
	private static boolean tryPlayerPalmAttack(ServerLevel level, Cat cat) {
		Player target = findTargetPlayer(cat);
		if (target == null) {
			return false;
		}
		return chaseAndStrikePalm(level, cat, target);
	}

	/**
	 * 已驯服猫对玩家以外目标的撼地掌（恐吓 10% / 攻击 90%）：
	 * 目标优先级——主人正在攻击的生物 &gt; 猫当前仇恨目标 &gt; 最近的敌对生物。
	 */
	private static boolean tryTamedPalmAttack(ServerLevel level, Cat cat) {
		LivingEntity target = findTamedTarget(cat);
		if (target == null) {
			return false;
		}
		return chaseAndStrikePalm(level, cat, target);
	}

	/**
	 * 已驯服猫目标选择：优先主人正在攻击的生物（狼式协同），其次猫当前仇恨目标
	 * （原版 AI：幻翼/苦力怕等），最后 16 格内最近的敌对生物。
	 */
	private static LivingEntity findTamedTarget(Cat cat) {
		LivingEntity ownerTarget = null;
		if (cat.getOwner() instanceof Player owner) {
			ownerTarget = owner.getLastHurtMob();
		}
		if (isValidPalmTarget(cat, ownerTarget)) {
			return ownerTarget;
		}

		LivingEntity current = cat.getTarget();
		if (isValidPalmTarget(cat, current)) {
			return current;
		}

		return findNearestMonster(cat);
	}

	/** 目标有效判定：存活、非自身、非玩家、16 格球形内且视线可见。 */
	private static boolean isValidPalmTarget(Cat cat, LivingEntity target) {
		return target != null
			&& target.isAlive()
			&& target != cat
			&& !(target instanceof Player)
			&& cat.distanceToSqr(target) <= PLAYER_RANGE * PLAYER_RANGE
			&& cat.hasLineOfSight(target);
	}

	private static LivingEntity findNearestMonster(Cat cat) {
		List<Monster> monsters = cat.level().getEntitiesOfClass(
			Monster.class,
			new AABB(cat.blockPosition()).inflate(PLAYER_RANGE),
			monster -> isValidPalmTarget(cat, monster)
		);
		if (monsters.isEmpty()) {
			return null;
		}
		return monsters.stream().min(Comparator.comparingDouble(cat::distanceToSqr)).orElse(null);
	}

	/** 逼近目标并适时施放撼地掌（含冷却等待）。 */
	private static boolean chaseAndStrikePalm(ServerLevel level, Cat cat, LivingEntity target) {
		double distanceSqr = cat.distanceToSqr(target);
		if (distanceSqr > PALM_ATTACK_RANGE_SQR) {
			cat.getNavigation().moveTo(target, 1.0);
			cat.getLookControl().setLookAt(target, 30.0F, 30.0F);
			return true;
		}

		cat.getNavigation().stop();
		cat.getLookControl().setLookAt(target, 30.0F, 30.0F);

		if (CatPartners.getHitgroundCooldown(cat) > 0) {
			// 攻击间隔冷却中：原地等待，不出击
			return true;
		}

		strikePalm(level, cat, target);
		return true;
	}

	/**
	 * 16 格内最近的可攻击玩家：非创造/旁观、视线可见；坏猫/键帽主动索敌，
	 * 普通猫仅在被该玩家攻击后的反击窗口内索敌。
	 */
	private static Player findTargetPlayer(Cat cat) {
		// 坏猫/键帽（好猫值 <40）主动攻击玩家；普通猫仅反击
		boolean aggressive = GoodCatLogic.getGoodValue(cat) < GoodCatLogic.BAD_THRESHOLD;
		List<Player> players = cat.level().getEntitiesOfClass(
			Player.class,
			new AABB(cat.blockPosition()).inflate(PLAYER_RANGE),
			player -> player.isAlive()
				&& !player.isCreative()
				&& !player.isSpectator()
				&& cat.distanceToSqr(player) <= PLAYER_RANGE * PLAYER_RANGE
				&& cat.hasLineOfSight(player)
				&& (aggressive || isRecentPlayerAggressor(cat, player))
		);
		if (players.isEmpty()) {
			return null;
		}
		return players.stream().min(Comparator.comparingDouble(cat::distanceToSqr)).orElse(null);
	}

	/** 普通猫反击判定：最近一次受伤来自该玩家且在反击窗口内。 */
	private static boolean isRecentPlayerAggressor(Cat cat, Player player) {
		return cat.getLastHurtByMob() == player
			&& cat.tickCount - cat.getLastHurtByMobTimestamp() < RETALIATION_WINDOW_TICKS;
	}

	/**
	 * 施放老吴撼地掌：进入 HITGROUND 状态播放动画（0.5s），并立即判定伤害。
	 * 恐吓类（未驯服 = 好猫值/100，已驯服 = 10%）不造成伤害；
	 * 攻击类造成 {@code (100-好猫值)*0.1} 伤害，键帽额外 20% 概率附带凋零 I 5 秒。
	 */
	private static void strikePalm(ServerLevel level, Cat cat, LivingEntity target) {
		CatPartners.setHitgroundTimer(cat, PALM_DURATION_TICKS);
		CatPartners.setHitgroundAnimTick(cat, cat.tickCount);
		CatPartners.setHitgroundCooldown(cat, PALM_COOLDOWN_TICKS);
		transitionTo(cat, CatState.HITGROUND);

		CatAudio.playHaSound(cat);
		if (GoodCatLogic.isIntimidate(cat)) {
			// 恐吓类：不对目标造成伤害
			return;
		}

		target.hurtServer(level, cat.damageSources().mobAttack(cat), GoodCatLogic.palmDamage(cat));
		if (GoodCatLogic.isKeycap(cat) && cat.getRandom().nextFloat() < GoodCatLogic.KEYCAP_WITHER_CHANCE) {
			target.addEffect(new MobEffectInstance(MobEffects.WITHER, GoodCatLogic.KEYCAP_WITHER_DURATION, 0), cat);
		}
	}

	/** 撼地掌动画期间：停导航、计时递减，归零后恢复 common。 */
	private static void hitgroundTick(ServerLevel level, Cat cat) {
		cat.getNavigation().stop();
		int timer = CatPartners.getHitgroundTimer(cat);
		if (timer > 0) {
			CatPartners.setHitgroundTimer(cat, timer - 1);
		} else {
			transitionTo(cat, CatState.COMMON);
		}
	}
}
