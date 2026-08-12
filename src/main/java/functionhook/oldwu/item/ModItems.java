package functionhook.oldwu.item;

import java.util.List;
import java.util.function.Function;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.food.FoodProperties;

import functionhook.oldwu.Old_Wu_java;
import functionhook.oldwu.block.ModBlocks;

public final class ModItems {
	public static final ResourceKey<Item> PAPER_ROLL_KEY = ResourceKey.create(Registries.ITEM, Old_Wu_java.id("paper_roll"));
	public static final ResourceKey<Item> DAGOUJIAO_KEY = ResourceKey.create(Registries.ITEM, Old_Wu_java.id("dagoujiao"));
	public static final ResourceKey<Item> GOUNAI_KEY = ResourceKey.create(Registries.ITEM, Old_Wu_java.id("gounai"));
	public static final ResourceKey<Item> CHUNQIU_CHANG_KEY = ResourceKey.create(Registries.ITEM, Old_Wu_java.id("chunqiu_chang"));

	public static final Item PAPER_ROLL = register(PAPER_ROLL_KEY, PaperRollItem::new, new Item.Properties().stacksTo(67));
	public static final Item DAGOUJIAO = register(DAGOUJIAO_KEY, Item::new, new Item.Properties().stacksTo(64));
	public static final Item GOUNAI = register(
		GOUNAI_KEY,
		GounaiItem::new,
		new Item.Properties()
			.stacksTo(64)
			.component(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
			.component(DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK)
			.component(DataComponents.FOOD, new FoodProperties.Builder().nutrition(6).saturationModifier(0.5F).alwaysEdible().build())
			.component(
				DataComponents.LORE,
				new ItemLore(List.of(Component.literal("保质期：永久"), Component.literal("就连时间也惧怕它的存在")))
			)
	);

	public static final Item CHUNQIU_CHANG = register(
		CHUNQIU_CHANG_KEY,
		ChunqiuChangItem::new,
		new Item.Properties()
			.stacksTo(64)
			.food(
				new FoodProperties.Builder().nutrition(6).saturationModifier(0.5F).alwaysEdible().build(),
				Consumables.defaultFood()
					.onConsume(
						new ApplyStatusEffectsConsumeEffect(
							List.of(
								new MobEffectInstance(MobEffects.POISON, 60, 0),
								new MobEffectInstance(MobEffects.NAUSEA, 100, 0)
							)
						)
					)
					.build()
			)
			.component(
				DataComponents.LORE,
				new ItemLore(List.of(Component.literal("生产日期：2018/1/1"), Component.literal("保质期：2008/1/1")))
			)
	);

	// 模组专属创造模式标签页
	public static final ResourceKey<CreativeModeTab> MOD_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Old_Wu_java.id("old_wu"));

	public static final CreativeModeTab MOD_TAB = Registry.register(
		BuiltInRegistries.CREATIVE_MODE_TAB,
		MOD_TAB_KEY,
		FabricCreativeModeTab.builder()
			.title(Component.translatable("itemGroup.old_wu_java.old_wu"))
			.icon(() -> new ItemStack(PAPER_ROLL))
			.displayItems((parameters, output) -> {
				output.accept(PAPER_ROLL);
				output.accept(DAGOUJIAO);
				output.accept(GOUNAI);
				output.accept(CHUNQIU_CHANG);
				output.accept(ModBlocks.MIRROR);
			})
			.build()
	);

	private ModItems() {
	}

	public static Item register(ResourceKey<Item> itemKey, Function<Item.Properties, Item> itemFactory, Item.Properties properties) {
		Item item = itemFactory.apply(properties.setId(itemKey));
		Registry.register(BuiltInRegistries.ITEM, itemKey, item);
		return item;
	}

	public static void initialize() {
	}
}
