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
 * 缠斗状态粒子，使用 {@code textures/particle/maomao.png}。
 */
public class MaomaoParticle extends SingleQuadParticle {
	private final SpriteSet sprites;

	private MaomaoParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites) {
		super(level, x, y, z, xd, yd, zd, sprites.first());
		this.sprites = sprites;
		this.lifetime = 20;
		this.quadSize = 0.12F;
		this.hasPhysics = false;
		this.friction = 0.9F;
		this.xd *= 0.03F;
		this.yd *= 0.03F;
		this.zd *= 0.03F;
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
			return new MaomaoParticle(level, x, y, z, xd, yd, zd, this.sprites);
		}
	}
}
