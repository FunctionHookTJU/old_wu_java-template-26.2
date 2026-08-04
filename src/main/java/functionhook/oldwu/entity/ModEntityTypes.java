package functionhook.oldwu.entity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import functionhook.oldwu.Old_Wu_java;

public final class ModEntityTypes {
	public static final EntityType<PaperRoll> PAPER_ROLL = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		Old_Wu_java.id("paper_roll"),
		EntityType.Builder.<PaperRoll>of(PaperRoll::new, MobCategory.MISC)
			.noLootTable()
			.sized(0.5F, 0.5F)
			.clientTrackingRange(4)
			.updateInterval(10)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, Old_Wu_java.id("paper_roll")))
	);

	private ModEntityTypes() {
	}

	public static void init() {
	}
}
