package functionhook.oldwu.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * 回血状态粒子，使用 {@code textures/particle/recovery.png}。
 */
public class RecoveryParticle extends SingleQuadParticle {
	private final SpriteSet sprites;

	private RecoveryParticle(
		ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites
	) {
		super(level, x, y, z, xd, yd, zd, sprites.first());
		this.sprites = sprites;
		this.lifetime = 30;
		this.quadSize = 0.14F;
		this.hasPhysics = false;
		this.friction = 0.9F;
		this.xd *= 0.02F;
		this.yd *= 0.02F;
		this.zd *= 0.02F;
		this.yd += 0.015F;
		this.alpha = 0.9F;
	}

	@Override
	public SingleQuadParticle.Layer getLayer() {
		return SingleQuadParticle.Layer.TRANSLUCENT;
	}

	@Override
	public void tick() {
		super.tick();
		this.setSpriteFromAge(this.sprites);
		this.alpha = 0.9F * Mth.clamp(1.0F - (float) this.age / this.lifetime, 0.0F, 1.0F);
	}

	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Provider(SpriteSet sprites) {
			this.sprites = sprites;
		}

		@Override
		public Particle createParticle(
			SimpleParticleType options,
			ClientLevel level,
			double x,
			double y,
			double z,
			double xd,
			double yd,
			double zd,
			RandomSource random
		) {
			return new RecoveryParticle(level, x, y, z, xd, yd, zd, this.sprites);
		}
	}
}
