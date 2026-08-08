package functionhook.oldwu.attribute;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import functionhook.oldwu.Old_Wu_java;

/**
 * 自定义属性注册。
 *
 * <p>{@code EXTRA_MAX_HEALTH}：额外最大生命值（无上限），用于突破原版
 * {@code minecraft:generic.max_health} 的 1024 上限，让"大狗叫"喂食 64 次能
 * 达到设计所需的最大生命值。该属性被添加到狼（Wolf）的默认属性中，并由
 * {@code LivingEntity#getMaxHealth} 注入叠加。
 *
 * <p>{@code CHARGE}：大狗叫蓄力进度（0~12），喂满 64 次后每次蓄力 +1，同步到
 * 客户端供喂食进度 HUD 的绿色蓄力条显示。
 */
public final class ModAttributes {
	public static final Holder<Attribute> EXTRA_MAX_HEALTH = register("extra_max_health", 0.0, 0.0, Double.MAX_VALUE, true);
	public static final Holder<Attribute> CHARGE = register("charge", 0.0, 0.0, 12.0, true);

	private static Holder<Attribute> register(String name, double defaultValue, double minValue, double maxValue, boolean syncedWithClient) {
		Identifier identifier = Old_Wu_java.id(name);
		Attribute entityAttribute = new RangedAttribute(
			identifier.toLanguageKey(),
			defaultValue,
			minValue,
			maxValue
		).setSyncable(syncedWithClient);
		return Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, identifier, entityAttribute);
	}

	public static void initialize() {
		FabricDefaultAttributeRegistry.MODIFY.register(context ->
			context.modify(EntityTypes.WOLF, (type, builder) -> {
				builder.add(EXTRA_MAX_HEALTH, 0.0);
				builder.add(CHARGE, 0.0);
			})
		);
	}

	private ModAttributes() {
	}
}
