package functionhook.oldwu.cat;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.feline.Cat;

/**
 * 猫养成等级系统：
 * <ul>
 *   <li>等级 {@link #MAX_LEVEL}（Lv1 起步），只有<b>已驯服</b>的猫能通过喂鱼获得经验并升级
 *       （坏猫/键帽好猫值 &lt; 40 无法被驯服，故无法喂鱼涨经验）。</li>
 *   <li>升级所需经验 {@link #expToNext}：前期快、后期慢的幂函数曲线。</li>
 *   <li>喂鱼经验：生鳕鱼/生鲑鱼 {@link #RAW_FISH_EXP}、熟鳕鱼/熟鲑鱼 {@link #COOKED_FISH_EXP}（更多）。</li>
 *   <li>血量：{@code maxHealth = 好猫值基础血量 + (等级-1) * HP_PER_LEVEL * (1 + 好猫值倍率)}，
 *       <b>等级决定每级增量、好猫值作为放大倍率</b>——好猫值越高，同样等级下血量越好。</li>
 *   <li>攻击力：撼地掌伤害在原有 {@code GoodCatLogic#palmDamage} 基础上按
 *       {@code (1 + (等级-1) * ATTACK_PER_LEVEL * (1 + 好猫值倍率))} 放大。</li>
 * </ul>
 *
 * <p>等级/经验分别持久化到 NBT（见 {@code CatMixin}），并经 {@code SynchedEntityData}
 * 同步到客户端供 HUD 显示。数值常量集中在顶部便于调平衡。
 */
public final class CatLeveling {
	/** 等级上限。 */
	public static final int MAX_LEVEL = 100;
	/** 未分配等级哨兵（实际应恒 ≥1）。 */
	public static final int BASE_LEVEL = 1;
	/** 喂生鳕鱼/生鲑鱼获得的经验。 */
	public static final int RAW_FISH_EXP = 25;
	/** 喂熟鳕鱼/熟鲑鱼获得的经验（更多）。 */
	public static final int COOKED_FISH_EXP = 60;

	/** 升级经验曲线基数。 */
	private static final double EXP_BASE = 15.0;
	/** 升级经验曲线斜率。 */
	private static final double EXP_GROWTH = 1.5;
	/** 升级经验曲线指数（前期慢后期快）。 */
	private static final double EXP_EXPONENT = 1.3;

	/** 每级增加的血量（在好猫值倍率放大前）。 */
	public static final double HP_PER_LEVEL = 0.5;
	/** 每级增加的攻击力系数（在好猫值倍率放大前）。 */
	public static final double ATTACK_PER_LEVEL = 0.01;

	private CatLeveling() {
	}

	/** 好猫值作为固定倍率（0~1）。 */
	public static float goodValueFactor(Cat cat) {
		int value = GoodCatLogic.getGoodValue(cat);
		if (value < 0) {
			return 0.0F;
		}
		return Math.min(1.0F, value / (float) GoodCatLogic.MAX_VALUE);
	}

	// ---------- 等级 / 经验 ----------

	public static int getLevel(Cat cat) {
		return CatPartners.getLevel(cat);
	}

	public static void setLevel(Cat cat, int level) {
		CatPartners.setLevel(cat, Math.max(BASE_LEVEL, Math.min(MAX_LEVEL, level)));
	}

	public static int getExp(Cat cat) {
		return CatPartners.getExp(cat);
	}

	public static void setExp(Cat cat, int exp) {
		CatPartners.setExp(cat, Math.max(0, exp));
	}

	/** 从当前等级升到下一级所需的经验。 */
	public static int expToNext(int level) {
		return (int) Math.floor(EXP_BASE + EXP_GROWTH * Math.pow(level, EXP_EXPONENT));
	}

	/**
	 * 添加经验并处理升级（服务端调用）。
	 * <p>经验积攒满足 {@link #expToNext} 时逐级扣除并升级，可连环升多级；
	 * 每次升级补满血量、播放经验音效与升级粒子。达到 {@link #MAX_LEVEL} 后不再累积。
	 *
	 * @return 本次升级的级数（0 表示未升级）
	 */
	public static int addExp(Cat cat, int amount) {
		if (cat.level().isClientSide()) {
			return 0;
		}
		int level = getLevel(cat);
		int exp = getExp(cat) + Math.max(0, amount);
		int levelsGained = 0;
		while (level < MAX_LEVEL && exp >= expToNext(level)) {
			exp -= expToNext(level);
			level++;
			levelsGained++;
		}
		if (level >= MAX_LEVEL) {
			exp = 0;
		}
		setLevel(cat, level);
		setExp(cat, exp);

		if (levelsGained > 0) {
			onLevelUp(cat, levelsGained);
		}
		return levelsGained;
	}

	/** 升级反馈：先应用新等级的血量上限，再补满血、播放经验音效与粒子。 */
	private static void onLevelUp(Cat cat, int levelsGained) {
		applyStatBonuses(cat);
		cat.setHealth(cat.getMaxHealth());
		if (cat.level() instanceof ServerLevel serverLevel) {
			serverLevel.playSound(
				null,
				cat.blockPosition(),
				SoundEvents.EXPERIENCE_ORB_PICKUP,
				SoundSource.NEUTRAL,
				1.0F,
				1.0F + 0.1F * Math.min(levelsGained, 5)
			);
			for (int i = 0; i < Math.min(20, levelsGained * 3); i++) {
				serverLevel.sendParticles(
					ParticleTypes.HAPPY_VILLAGER,
					cat.getX(), cat.getY() + cat.getBbHeight(), cat.getZ(),
					1, cat.getRandom().nextDouble() * 0.5, cat.getRandom().nextDouble() * 0.5, cat.getRandom().nextDouble() * 0.5, 0.0
				);
			}
		}
	}

	// ---------- 属性加成 ----------

	/**
	 * 应用等级血量加成（服务端 tick 调用；maodie 由 {MaodieLogic} 单独管理，不在此处理）。
	 * <p>已驯服猫在好猫值基础血量（绝世 40 / 普通 25）之上叠加等级成长；未驯服/未升级猫
	 * （等级 1，等级增量为 0）不会产生等级加成，故不会改变原版/键帽/驯服逻辑。
	 */
	public static void applyStatBonuses(Cat cat) {
		if (CatMatingLogic.isMaodie(cat)) {
			return;
		}
		var maxHealth = cat.getAttribute(Attributes.MAX_HEALTH);
		if (maxHealth == null) {
			return;
		}

		double target;
		if (cat.isTame()) {
			double tamedBase = GoodCatLogic.isPeerless(cat)
				? GoodCatLogic.TAMED_PEERLESS_MAX_HEALTH
				: GoodCatLogic.TAMED_NORMAL_MAX_HEALTH;
			// 等级决定基准增量，好猫值作为放大倍率
			target = tamedBase + (getLevel(cat) - 1) * HP_PER_LEVEL * (1.0 + goodValueFactor(cat));
		} else {
			// 未驯服（含键帽，由 applyKeycapBonuses 负责 30 血/10 防）不叠加等级，保持基础
			if (GoodCatLogic.isKeycap(cat)) {
				return;
			}
			target = maxHealth.getBaseValue(); // 原版猫基础血量（默认 10），无等级加成
		}

		if (maxHealth.getBaseValue() != target) {
			maxHealth.setBaseValue(target);
			cat.setHealth(cat.getMaxHealth());
		}
	}

	/** 攻击力倍率：等级决定增量、好猫值作为放大倍率。 */
	public static float attackMultiplier(Cat cat) {
		double levelPart = (getLevel(cat) - 1) * ATTACK_PER_LEVEL;
		return (float) (1.0 + levelPart * (1.0 + goodValueFactor(cat)));
	}

	/** 撼地掌伤害：在原有好猫值伤害基础上叠加等级倍率。 */
	public static float palmDamage(Cat cat) {
		return GoodCatLogic.palmDamage(cat) * attackMultiplier(cat);
	}
}
