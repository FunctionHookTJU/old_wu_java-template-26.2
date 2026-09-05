package functionhook.oldwu.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.item.Item;

import functionhook.oldwu.cat.CatLeveling;

/**
 * 猫粮：用于喂食<b>已驯服</b>的猫，提供回血 + 大量经验（远超普通喂鱼）。
 * <ul>
 *   <li>普通猫粮（{@code catfood1}）：小回血 + 一定经验 {@link FoodType#NORMAL}。</li>
 *   <li>高级猫粮（{@code catfood2}）：中回血 + 更多经验 {@link FoodType#ADVANCED}。</li>
 *   <li>至尊猫粮（{@code catfood3}）：大回血 + 大量经验 {@link FoodType#SUPREME}。</li>
 *   <li>创造猫粮（{@code catfood_create}）：{@link FoodType#CREATIVE}——喂食直接升到满级
 *       （经验视为无穷）并回满 10000 点血（对猫来说等同全满）。</li>
 * </ul>
 *
 * <p>喂食交互在 {@code CatMixin.mobInteract} 里注入处理：仅<b>已驯服</b>猫可被喂猫粮，
 * 客户端返回 SUCCESS 触发挥臂，服务端执行回血与加经验并消耗 1 个猫粮。
 */
public class CatFoodItem extends Item {
	/** 猫粮档位：决定回血与经验量。 */
	public enum FoodType {
		NORMAL(6.0F, 200),
		ADVANCED(12.0F, 550),
		SUPREME(20.0F, 1500),
		/** 创造猫粮：经验视为无穷（直接满级），并回复 10000 点血。 */
		CREATIVE(10000.0F, 0);

		public final float heal;
		public final int exp;

		FoodType(float heal, int exp) {
			this.heal = heal;
			this.exp = exp;
		}
	}

	private final FoodType type;

	public CatFoodItem(Properties properties, FoodType type) {
		super(properties);
		this.type = type;
	}

	public FoodType getType() {
		return this.type;
	}

	/**
	 * 喂食效果（服务端调用）：按档位回血 + 加经验。
	 * <p>创造猫粮：等级直接升到 {@link CatLeveling#MAX_LEVEL}（经验视为无穷）、经验清零、
	 * 应用满级属性并回满血。
	 */
	public void feed(Cat cat) {
		cat.heal(this.type.heal);
		if (this.type == FoodType.CREATIVE) {
			CatLeveling.setLevel(cat, CatLeveling.MAX_LEVEL);
			CatLeveling.setExp(cat, 0);
			CatLeveling.applyStatBonuses(cat);
			cat.setHealth(cat.getMaxHealth());
			if (cat.level() instanceof ServerLevel serverLevel) {
				serverLevel.playSound(null, cat.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1.0F, 1.0F);
				for (int i = 0; i < 30; i++) {
					serverLevel.sendParticles(
						ParticleTypes.HAPPY_VILLAGER,
						cat.getX(), cat.getY() + cat.getBbHeight(), cat.getZ(),
						1, cat.getRandom().nextDouble() * 0.6, cat.getRandom().nextDouble() * 0.6, cat.getRandom().nextDouble() * 0.6, 0.0
					);
				}
			}
		} else {
			CatLeveling.addExp(cat, this.type.exp);
		}
	}
}
