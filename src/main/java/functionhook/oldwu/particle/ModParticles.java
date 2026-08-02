package functionhook.oldwu.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

import functionhook.oldwu.Old_Wu_java;

public final class ModParticles {
	public static final SimpleParticleType RECOVERY = FabricParticleTypes.simple();
	public static final SimpleParticleType MAOMAO = FabricParticleTypes.simple();

	private ModParticles() {
	}

	public static void init() {
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, Old_Wu_java.id("recovery"), RECOVERY);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, Old_Wu_java.id("maomao"), MAOMAO);
	}
}
