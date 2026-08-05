package functionhook.oldwu.cat;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import functionhook.oldwu.accessor.MobTargetAccessor;
import functionhook.oldwu.audio.CatAudio;
import functionhook.oldwu.entity.ModEntityTypes;
import functionhook.oldwu.entity.PaperRoll;

/**
 * 被命名为 "maodie" 的猫的特殊行为逻辑：
 * <ul>
 *   <li>血量上限 325；使用原版 {@code generic.scale} 属性放大 1.5 倍（模型+碰撞箱）</li>
 *   <li>主动攻击玩家（优先）；无玩家在范围内时无差别攻击周围所有生物</li>
 *   <li>没有任何攻击目标时随机游荡</li>
 *   <li>攻击/发射纸筒时贴图切换为 haqi，并播放 ha 系列音效</li>
 *   <li>攻击伤害 5~20 随机；0.2 概率中毒 II 5 秒、0.1 概率中毒 II + 凋零 II 8 秒；攻击时产生两个爆炸粒子</li>
 *   <li>身后有 #CCA675 色尘粒子构成的常驻圆环</li>
 *   <li>血量 ≤114 进入狂暴：每 100 tick 向目标发射一枚全速纸筒（发射前 5 tick 起显示 haqi）</li>
 *   <li>击败后掉落 2~5 个纸筒，掉落数量受抢夺附魔影响（在 LivingEntityMixin 处理）</li>
 * </ul>
 */
public final class MaodieLogic {
	public static final double MAX_HEALTH = 325.0;
	// 原版 generic.scale 属性放大倍率（等价于 /attribute ... generic.scale base set 1.5）
	public static final double MAODIE_SCALE = 1.5;
	public static final double RAGE_THRESHOLD = 114.0;
	public static final int RAGE_FIRE_INTERVAL = 80;
	public static final int RAGE_WINDUP_TICKS = 5;
	public static final float MIN_ATTACK_DAMAGE = 5.0F;
	public static final float MAX_ATTACK_DAMAGE = 20.0F;
	public static final int POISON_DURATION = 100;
	public static final int COMBO_DURATION = 160;
	public static final float POISON_CHANCE = 0.2F;
	public static final float COMBO_CHANCE = 0.1F;
	public static final double TARGET_RANGE = 16.0;
	// 攻击距离 3 格（原 2 格 + 1）
	public static final double ATTACK_RANGE_SQR = 9.0;
	public static final double MOVE_SPEED = 1.0;
	// 无目标时随机游荡的间隔与半径
	public static final int WANDER_INTERVAL_TICKS = 40;
	public static final double WANDER_RADIUS = 8.0;
	public static final int MELEE_ATTACK_MIN_DELAY = 20;
	public static final int MELEE_ATTACK_MAX_DELAY = 40;
	// 攻击后 haqi 贴图持续时长（tick），与挥击动画时长大致匹配
	public static final int ATTACK_HAQI_TICKS = 12;
	public static final float ROLL_SPEED = 4.0F;
	// 身后常驻圆环粒子（#CCA675，DustParticleOptions 使用 RGB24）
	public static final int RING_COLOR = 0xCCA675;
	public static final int RING_PARTICLES_PER_TICK = 6;
	public static final double RING_RADIUS = 1.0;
	public static final double RING_DISTANCE = 1.2;
	public static final double RING_HEIGHT = 1.0;
	// Boss 血条：原版无 #CCA675 颜色，使用相近的黄色
	public static final BossBarColor BOSS_BAR_COLOR = BossBarColor.YELLOW;
	private static final Map<UUID, ServerBossEvent> BOSS_EVENTS = new HashMap<>();

	private MaodieLogic() {
	}

	public static void tick(ServerLevel level, Cat cat) {
		if (!cat.isAlive()) {
			return;
		}

		initIfNeeded(cat);

		// 更新 Boss 血条（黄色），并同步给本维度所有玩家
		ensureBossBar(level, cat);

		// 近战冷却即使不在攻击范围内也持续下降
		int attackCooldown = CatPartners.getAttackCooldown(cat);
		if (attackCooldown > 0) {
			CatPartners.setAttackCooldown(cat, attackCooldown - 1);
		}

		// haqi 计时递减（贴图切换持续数 tick 而非单帧闪烁）
		int haqi = CatPartners.getMaodieHaqiTimer(cat);
		if (haqi > 0) {
			CatPartners.setMaodieHaqiTimer(cat, haqi - 1);
		}

		// 身后常驻 #CCA675 圆环粒子
		spawnRingParticles(level, cat);

		LivingEntity target = findTarget(cat);
		if (target == null) {
			// 无目标：随机游荡
			wanderTick(cat);
			return;
		}

		boolean attacked = false;
		if (cat.distanceToSqr(target) > ATTACK_RANGE_SQR) {
			cat.getNavigation().moveTo(target, MOVE_SPEED);
			cat.getLookControl().setLookAt(target, 30.0F, 30.0F);
		} else {
			cat.getNavigation().stop();
			cat.getLookControl().setLookAt(target, 30.0F, 30.0F);
			attacked = meleeAttack(level, cat, target);
		}

		// 狂暴：血量 ≤114 时每 100 tick 发射纸筒；蓄力/发射期间延长 haqi
		if (cat.getHealth() <= RAGE_THRESHOLD) {
			if (rageTick(level, cat, target)) {
				extendHaqi(cat, RAGE_WINDUP_TICKS + 1);
			}
		} else {
			CatPartners.setMaodieRageCooldown(cat, 0);
		}

		// 近战攻击瞬间延长 haqi（覆盖攻击动画时长）
		if (attacked) {
			extendHaqi(cat, ATTACK_HAQI_TICKS);
		}
	}

	/**
	 * 目标选择：优先最近的玩家；无玩家时无差别攻击范围内的所有生物。
	 */
	private static LivingEntity findTarget(Cat cat) {
		Player player = findNearestPlayer(cat);
		if (player != null) {
			return player;
		}
		return findNearestLiving(cat);
	}

	private static Player findNearestPlayer(Cat cat) {
		List<Player> players = cat.level().getEntitiesOfClass(
			Player.class,
			new AABB(cat.blockPosition()).inflate(TARGET_RANGE),
			player -> player.isAlive() && !player.isCreative() && !player.isSpectator()
		);
		if (players.isEmpty()) {
			return null;
		}
		return players.stream().min(Comparator.comparingDouble(cat::distanceToSqr)).orElse(null);
	}

	private static LivingEntity findNearestLiving(Cat cat) {
		List<LivingEntity> entities = cat.level().getEntitiesOfClass(
			LivingEntity.class,
			new AABB(cat.blockPosition()).inflate(TARGET_RANGE),
			entity -> entity != cat
				&& entity.isAlive()
				&& !(entity instanceof ArmorStand)
				&& !(entity instanceof Player)
				&& !(entity instanceof Cat other && CatMatingLogic.isMaodie(other))
		);
		if (entities.isEmpty()) {
			return null;
		}
		return entities.stream().min(Comparator.comparingDouble(cat::distanceToSqr)).orElse(null);
	}

	/**
	 * 首次变为 maodie 时：提升血量上限至 325、补满血，并清空原版 AI 目标，
	 * 保证 maodie 只执行本逻辑（仍保留导航/移动能力）。
	 */
	private static void initIfNeeded(Cat cat) {
		var attribute = cat.getAttribute(Attributes.MAX_HEALTH);
		if (attribute != null && attribute.getBaseValue() != MAX_HEALTH) {
			attribute.setBaseValue(MAX_HEALTH);
			cat.setHealth(cat.getMaxHealth());
		}

		// 使用原版 generic.scale 属性放大（模型与碰撞箱一起缩放，并同步给客户端）
		var scale = cat.getAttribute(Attributes.SCALE);
		if (scale != null && scale.getBaseValue() != MAODIE_SCALE) {
			scale.setBaseValue(MAODIE_SCALE);
			cat.refreshDimensions();
		}

		if (!cat.getGoalSelector().getAvailableGoals().isEmpty()) {
			cat.getGoalSelector().removeAllGoals(goal -> true);
		}
		MobTargetAccessor accessor = (MobTargetAccessor) (Object) cat;
		if (!accessor.oldwu_getTargetSelector().getAvailableGoals().isEmpty()) {
			accessor.oldwu_getTargetSelector().removeAllGoals(goal -> true);
		}
	}

	private static void extendHaqi(Cat cat, int ticks) {
		int current = CatPartners.getMaodieHaqiTimer(cat);
		if (current < ticks) {
			CatPartners.setMaodieHaqiTimer(cat, ticks);
		}
	}

	/**
	 * 创建/更新耄耋的 Boss 血条并加入本维度所有玩家为观察者。
	 */
	private static void ensureBossBar(ServerLevel level, Cat cat) {
		ServerBossEvent event = BOSS_EVENTS.computeIfAbsent(
			cat.getUUID(),
			id -> new ServerBossEvent(id, Component.translatable("entity.old_wu_java.maodie"), BOSS_BAR_COLOR, BossBarOverlay.PROGRESS)
		);
		float ratio = Math.max(0.0F, Math.min(1.0F, cat.getHealth() / cat.getMaxHealth()));
		event.setProgress(ratio);
		for (ServerPlayer player : level.players()) {
			event.addPlayer(player);
		}
	}

	/**
	 * 移除耄耋的 Boss 血条（死亡或被改名时调用）。
	 */
	public static void removeBossBar(Cat cat) {
		ServerBossEvent event = BOSS_EVENTS.remove(cat.getUUID());
		if (event != null) {
			event.removeAllPlayers();
		}
	}

	/**
	 * 无攻击目标时随机游荡：每 {@link #WANDER_INTERVAL_TICKS} tick 向随机方向移动一段距离。
	 */
	private static void wanderTick(Cat cat) {
		if (cat.tickCount % WANDER_INTERVAL_TICKS == 0) {
			double angle = cat.getRandom().nextDouble() * 2.0 * Math.PI;
			double radius = WANDER_RADIUS * (0.25 + cat.getRandom().nextDouble() * 0.75);
			double targetX = cat.getX() + Math.cos(angle) * radius;
			double targetZ = cat.getZ() + Math.sin(angle) * radius;
			cat.getNavigation().moveTo(targetX, cat.getY(), targetZ, MOVE_SPEED);
		}
	}

	/**
	 * 耄耋身后常驻的 #CCA675 粒子圆环：每 tick 在朝向反方向的竖直圆环上撒几颗色尘粒子。
	 */
	private static void spawnRingParticles(ServerLevel level, Cat cat) {
		Vec3 look = cat.getLookAngle();
		double horizontal = Math.sqrt(look.x * look.x + look.z * look.z);
		Vec3 behind;
		if (horizontal < 1.0E-4) {
			behind = new Vec3(0.0, 0.0, 1.0);
		} else {
			behind = new Vec3(-look.x / horizontal, 0.0, -look.z / horizontal);
		}
		Vec3 up = new Vec3(0.0, 1.0, 0.0);
		Vec3 right = behind.cross(up);

		Vec3 center = cat.position().add(behind.scale(RING_DISTANCE)).add(0.0, RING_HEIGHT, 0.0);
		for (int i = 0; i < RING_PARTICLES_PER_TICK; i++) {
			double theta = cat.getRandom().nextDouble() * 2.0 * Math.PI;
			double radius = RING_RADIUS * (0.85 + cat.getRandom().nextDouble() * 0.3);
			Vec3 offset = right.scale(Math.cos(theta) * radius).add(up.scale(Math.sin(theta) * radius));
			Vec3 pos = center.add(offset);
			level.sendParticles(new DustParticleOptions(RING_COLOR, 1.0F), pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
		}
	}

	/**
	 * 攻击时产生两个爆炸粒子（仅视觉效果，不产生真实爆炸）。
	 */
	private static void spawnExplosionParticles(ServerLevel level, Cat cat) {
		level.sendParticles(
			ParticleTypes.EXPLOSION,
			cat.getX(),
			cat.getY() + cat.getBbHeight() * 0.5,
			cat.getZ(),
			2,
			0.4,
			0.4,
			0.4,
			0.1
		);
	}

	/**
	 * @return 本 tick 是否发动了近战攻击（用于 haqi 贴图切换）
	 */
	private static boolean meleeAttack(ServerLevel level, Cat cat, LivingEntity target) {
		if (CatPartners.getAttackCooldown(cat) > 0) {
			return false;
		}

		float damage = MIN_ATTACK_DAMAGE + cat.getRandom().nextFloat() * (MAX_ATTACK_DAMAGE - MIN_ATTACK_DAMAGE);
		target.hurtServer(level, cat.damageSources().mobAttack(cat), damage);
		applyAttackEffects(cat, target);
		CatPartners.setAttackCooldown(cat, nextMeleeDelay(cat));
		// 攻击爆炸粒子（仅粒子，非真实爆炸）+ 音效 + 动画
		spawnExplosionParticles(level, cat);
		CatAudio.playHaSound(cat);
		CatPartners.setMaodieAnimTick(cat, cat.tickCount);
		return true;
	}

	// 中毒 II（5 秒）20%；中毒 II + 凋零 II（8 秒）10%
	private static void applyAttackEffects(Cat cat, LivingEntity target) {
		float roll = cat.getRandom().nextFloat();
		if (roll < COMBO_CHANCE) {
			target.addEffect(new MobEffectInstance(MobEffects.POISON, COMBO_DURATION, 1), cat);
			target.addEffect(new MobEffectInstance(MobEffects.WITHER, COMBO_DURATION, 1), cat);
		} else if (roll < POISON_CHANCE + COMBO_CHANCE) {
			target.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_DURATION, 1), cat);
		}
	}

	/**
	 * 狂暴模式：每 {@link #RAGE_FIRE_INTERVAL} tick 向目标发射一枚全速纸筒。
	 * 发射前 {@link #RAGE_WINDUP_TICKS} tick 起贴图切为 haqi。
	 *
	 * @return 是否处于蓄力/发射的 haqi 展示期间
	 */
	private static boolean rageTick(ServerLevel level, Cat cat, LivingEntity target) {
		int cooldown = CatPartners.getMaodieRageCooldown(cat);
		if (cooldown <= 0) {
			firePaperRoll(level, cat, target);
			CatPartners.setMaodieRageCooldown(cat, RAGE_FIRE_INTERVAL);
			return true;
		}

		CatPartners.setMaodieRageCooldown(cat, cooldown - 1);
		return cooldown <= RAGE_WINDUP_TICKS;
	}

	private static void firePaperRoll(ServerLevel level, Cat cat, LivingEntity target) {
		PaperRoll roll = new PaperRoll(ModEntityTypes.PAPER_ROLL, level);
		Vec3 spawn = cat.getEyePosition();
		roll.setPos(spawn.x, spawn.y, spawn.z);
		roll.setOwner(cat);
		Vec3 aim = target.getEyePosition().subtract(spawn).normalize();
		roll.shoot(aim.x, aim.y, aim.z, ROLL_SPEED, 0.0F);
		level.addFreshEntity(roll);
		// 发射纸筒音效（ha 系列，带字幕）+ 攻击动画
		CatAudio.playHaSound(cat);
		CatPartners.setMaodieAnimTick(cat, cat.tickCount);
	}

	private static int nextMeleeDelay(Cat cat) {
		return MELEE_ATTACK_MIN_DELAY + cat.getRandom().nextInt(MELEE_ATTACK_MAX_DELAY - MELEE_ATTACK_MIN_DELAY + 1);
	}
}
