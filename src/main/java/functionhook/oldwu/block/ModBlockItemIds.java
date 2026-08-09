package functionhook.oldwu.block;

import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;

import functionhook.oldwu.Old_Wu_java;

public final class ModBlockItemIds {
	public static final BlockItemId MIRROR = create("mirror");

	private static BlockItemId create(String name) {
		Identifier id = Identifier.fromNamespaceAndPath(Old_Wu_java.MOD_ID, name);
		return BlockItemId.create(id, id);
	}

	private ModBlockItemIds() {
	}
}
