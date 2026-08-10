package functionhook.oldwu.cat;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.feline.Cat;

/**
 * 好猫值（0~100）系统：
 * <ul>
 *   <li>每只猫（自然生成/命令召唤/刷怪蛋/繁殖）都有随机好猫值；未分配（-1）时随机 0~100。</li>
 *   <li>好猫值 0~19 键帽、20~39 坏猫、40~79 普通猫、80~100 绝世好猫。</li>
 *   <li>键帽：血量上限 +20、防御 +10（{@link #applyKeycapBonuses}）。</li>
 *   <li>驯服后好猫值 +10；繁殖后代 = min(100, max(父母) + 5)（在 {@code CatMixin} 处理）。</li>
 *   <li>老吴撼地掌：攻击类伤害 {@link #palmDamage}；恐吓类概率 {@link #isIntimidate}。</li>
 * </ul>
 */
public final class GoodCatLogic {
	/** 键帽最大好猫值（0~19）。 */
	public static final int KEYCAP_THRESHOLD = 20;
	/** 坏猫最大好猫值（20~39）。 */
	public static final int BAD_THRESHOLD = 40;
	/** 普通猫最大好猫值（40~79）。 */
	public static final int NORMAL_THRESHOLD = 80;
	/** 好猫值上限。 */
	public static final int MAX_VALUE = 100;
	/** 未分配哨兵。 */
	public static final int UNASSIGNED = -1;
	/** 键帽血量上限（原版猫 10 + 20）。 */
	public static final double KEYCAP_MAX_HEALTH = 30.0;
	/** 键帽防御（原版 0 + 10）。 */
	public static final double KEYCAP_ARMOR = 10.0;
	/** 已驯服普通猫血量上限（原版猫 10 + 15）。 */
	public static final double TAMED_NORMAL_MAX_HEALTH = 25.0;
	/** 已驯服绝世好猫血量上限（原版猫 10 + 30）。 */
	public static final double TAMED_PEERLESS_MAX_HEALTH = 40.0;
	/** 已驯服猫撼地掌恐吓概率（攻击概率为 1 - 该值）。 */
	public static final float TAMED_INTIMIDATE_CHANCE = 0.10F;
	/** 驯服后好猫值加成。 */
	public static final int TAME_BOOST = 10;
	/** 繁殖后代相对父母最大值的加成。 */
	public static final int BREED_BOOST = 5;
	/** 键帽攻击附带凋零 I 的概率。 */
	public static final float KEYCAP_WITHER_CHANCE = 0.2F;
	/** 键帽凋零 I 时长（tick）。 */
	public static final int KEYCAP_WITHER_DURATION = 100;

	private GoodCatLogic() {
	}

	public static int getGoodValue(Cat cat) {
		return CatPartners.getGoodValue(cat);
	}

	public static void setGoodValue(Cat cat, int value) {
		CatPartners.setGoodValue(cat, clamp(value));
	}

	public static int clamp(int value) {
		return Math.max(0, Math.min(MAX_VALUE, value));
	}

	/**
	 * 未分配（值 &lt; 0）时随机分配 0~100。
	 */
	public static void assignRandomIfAbsent(Cat cat) {
		if (getGoodValue(cat) < 0) {
			setGoodValue(cat, cat.getRandom().nextInt(MAX_VALUE + 1));
		}
	}

	/** 键帽：好猫值 0~19。 */
	public static boolean isKeycap(Cat cat) {
		return getGoodValue(cat) < KEYCAP_THRESHOLD;
	}

	/** 坏猫：好猫值 20~39。 */
	public static boolean isBad(Cat cat) {
		int v = getGoodValue(cat);
		return v >= KEYCAP_THRESHOLD && v < BAD_THRESHOLD;
	}

	/** 普通猫：好猫值 40~79。 */
	public static boolean isNormal(Cat cat) {
		int v = getGoodValue(cat);
		return v >= BAD_THRESHOLD && v < NORMAL_THRESHOLD;
	}

	/** 绝世好猫：好猫值 ≥80。 */
	public static boolean isPeerless(Cat cat) {
		return getGoodValue(cat) >= NORMAL_THRESHOLD;
	}

	/**
	 * 键帽加成：血量上限 30（+20）、防御 10，仅首次变化时补满血（幂等，仿 maodie initIfNeeded）。
	 */
	public static void applyKeycapBonuses(Cat cat) {
		if (!isKeycap(cat)) {
			return;
		}
		var maxHealth = cat.getAttribute(Attributes.MAX_HEALTH);
		if (maxHealth != null && maxHealth.getBaseValue() != KEYCAP_MAX_HEALTH) {
			maxHealth.setBaseValue(KEYCAP_MAX_HEALTH);
			cat.setHealth(cat.getMaxHealth());
		}
		var armor = cat.getAttribute(Attributes.ARMOR);
		if (armor != null && armor.getBaseValue() != KEYCAP_ARMOR) {
			armor.setBaseValue(KEYCAP_ARMOR);
		}
	}

	/**
	 * 已驯服猫血量上限：绝世好猫 40、普通猫 25（幂等，首次变化补满血）。
	 * 已驯服猫好猫值 ≥50，不可能是键帽/坏猫，不会与键帽加成冲突。
	 */
	public static void applyTamedBonuses(Cat cat) {
		if (!cat.isTame()) {
			return;
		}
		double target = isPeerless(cat) ? TAMED_PEERLESS_MAX_HEALTH : TAMED_NORMAL_MAX_HEALTH;
		var maxHealth = cat.getAttribute(Attributes.MAX_HEALTH);
		if (maxHealth != null && maxHealth.getBaseValue() != target) {
			maxHealth.setBaseValue(target);
			cat.setHealth(cat.getMaxHealth());
		}
	}

	/**
	 * 老吴撼地掌攻击类伤害：已驯服猫 = 好猫值 * 0.15；未驯服猫 = (100 - 好猫值) * 0.1。
	 */
	public static float palmDamage(Cat cat) {
		if (cat.isTame()) {
			return getGoodValue(cat) * 0.15F;
		}
		return (MAX_VALUE - getGoodValue(cat)) * 0.1F;
	}

	/**
	 * 恐吓类概率：已驯服猫固定 {@link #TAMED_INTIMIDATE_CHANCE}（攻击 90%）；
	 * 未驯服猫 = 好猫值 / 100，其余情况为攻击类。
	 */
	public static boolean isIntimidate(Cat cat) {
		if (cat.isTame()) {
			return cat.getRandom().nextFloat() < TAMED_INTIMIDATE_CHANCE;
		}
		return cat.getRandom().nextFloat() < getGoodValue(cat) / (float) MAX_VALUE;
	}
}
