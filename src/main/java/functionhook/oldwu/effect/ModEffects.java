package functionhook.oldwu.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

import functionhook.oldwu.Old_Wu_java;

/**
 * 模组自定义状态效果注册。
 *
 * <p>{@code ETERNAL}（永恒）：饮用野生狗奶后获得，无限时长；玩家拥有该效果时
 * 不会受到任何伤害、喝牛奶/蜂蜜也无法解除（具体行为见 {@code EternalEffectMixin}
 * 与客户端 {@code HudHeartMixin}）。
 */
public final class ModEffects {
	public static final Holder<MobEffect> ETERNAL =
		Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Old_Wu_java.id("eternal"), new EternalEffect());

	private ModEffects() {
	}

	/**
	 * 触发静态字段初始化（完成 MOB_EFFECT 注册）。
	 */
	public static void initialize() {
		Old_Wu_java.LOGGER.info("Registering {} mob effects", Old_Wu_java.MOD_ID);
	}
}
