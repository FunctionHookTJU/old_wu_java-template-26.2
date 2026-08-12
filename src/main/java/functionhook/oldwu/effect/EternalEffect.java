package functionhook.oldwu.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 永恒：饮用野生狗奶后获得的永久状态效果。
 * <ul>
 *   <li>玩家拥有该效果时不再受到任何伤害（连 /kill 指令也无法杀死，由
 *       {@code EternalEffectMixin} 拦截 {@code hurtServer} 实现）。</li>
 *   <li>喝牛奶/蜂蜜也无法解除该效果（由 {@code EternalEffectMixin} 拦截
 *       {@code removeEffect}/{@code removeAllEffects} 实现）。</li>
 *   <li>客户端 HUD 将血条的心替换为 infinite.png（由客户端 {@code HudHeartMixin} 实现）。</li>
 * </ul>
 */
public class EternalEffect extends MobEffect {
	public EternalEffect() {
		// 药水粒子效果的颜色：#2B2D30
		super(MobEffectCategory.BENEFICIAL, 0x2B2D30);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return false;
	}
}
