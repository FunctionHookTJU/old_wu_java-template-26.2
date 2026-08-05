package functionhook.oldwu.item;

import java.util.function.Function;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import functionhook.oldwu.Old_Wu_java;

public final class ModItems {
	public static final ResourceKey<Item> PAPER_ROLL_KEY = ResourceKey.create(Registries.ITEM, Old_Wu_java.id("paper_roll"));

	public static final Item PAPER_ROLL = register(PAPER_ROLL_KEY, PaperRollItem::new, new Item.Properties());

	// 模组专属创造模式标签页
	public static final ResourceKey<CreativeModeTab> MOD_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Old_Wu_java.id("old_wu"));

	public static final CreativeModeTab MOD_TAB = Registry.register(
		BuiltInRegistries.CREATIVE_MODE_TAB,
		MOD_TAB_KEY,
		FabricCreativeModeTab.builder()
			.title(Component.translatable("itemGroup.old_wu_java.old_wu"))
			.icon(() -> new ItemStack(PAPER_ROLL))
			.displayItems((parameters, output) -> output.accept(PAPER_ROLL))
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
