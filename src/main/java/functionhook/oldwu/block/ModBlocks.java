package functionhook.oldwu.block;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 方块注册。目前包含"镜子"方块（硬度 1、可空手挖掘）。
 */
public final class ModBlocks {
	public static final Block MIRROR = register(
		ModBlockItemIds.MIRROR,
		MirrorBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(net.minecraft.world.level.material.MapColor.COLOR_LIGHT_GRAY)
			.strength(1.0F)
			.sound(SoundType.GLASS)
			.noCollision()
			.noOcclusion()
	);

	private static Block register(BlockItemId id, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties properties) {
		// 注册方块本体
		Block block = register(id.block(), blockFactory, properties);

		// 注册对应的方块物品
		BlockItem blockItem = new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(id.item()));
		Registry.register(BuiltInRegistries.ITEM, id.item(), blockItem);

		return block;
	}

	private static Block register(ResourceKey<Block> blockKey, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties properties) {
		Block block = blockFactory.apply(properties.setId(blockKey));
		return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
	}

	public static void initialize() {
	}

	private ModBlocks() {
	}
}
