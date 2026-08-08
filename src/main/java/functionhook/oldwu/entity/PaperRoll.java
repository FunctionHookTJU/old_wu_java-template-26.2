package functionhook.oldwu.entity;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import functionhook.oldwu.cat.CatMatingLogic;

/**
 * 纸卷弹射物：直线飞行（无抛物线），击中玩家或方块后产生约恶魂火焰弹强度的爆炸（不产生火焰）。
 * <p>maodie 不会受到自己发射的纸卷的直接伤害，也不会受到其爆炸的伤害。
 */
public class PaperRoll extends AbstractHurtingProjectile {
	public static final float DAMAGE = 15.0F;
	public static final float EXPLOSION_POWER = 1.0F;

	public PaperRoll(EntityType<? extends PaperRoll> type, Level level) {
		super(type, level);
		// 无加速：直线匀速飞行
		this.accelerationPower = 0.0;
	}

	public PaperRoll(Level level, LivingEntity shooter, Vec3 direction) {
		super(ModEntityTypes.PAPER_ROLL, shooter, direction, level);
		this.accelerationPower = 0.0;
	}

	// 无空气阻力衰减，保持匀速直线飞行
	@Override
	protected float getInertia() {
		return 1.0F;
	}

	// 不燃烧
	@Override
	protected boolean shouldBurn() {
		return false;
	}

	// 无尾迹粒子
	@Override
	protected @org.jspecify.annotations.Nullable ParticleOptions getTrailParticle() {
		return null;
	}

	/**
	 * 是否为 maodie 发射的纸卷（发射者是命名为 maodie 的猫）。
	 */
	private static boolean isMaodieOwner(Entity owner) {
		return owner instanceof Cat cat && CatMatingLogic.isMaodie(cat);
	}

	// 固定 15 点弹射物伤害；maodie 免受自己纸卷的直接伤害
	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
		super.onHitEntity(hitResult);
		if (this.level() instanceof ServerLevel serverLevel) {
			Entity target = hitResult.getEntity();
			Entity owner = this.getOwner();
			if (isMaodieOwner(owner) && target == owner) {
				return;
			}
			DamageSource source = owner instanceof LivingEntity living
				? this.damageSources().mobProjectile(this, living)
				: this.damageSources().generic();
			target.hurtServer(serverLevel, source, DAMAGE);
		}
	}

	// 击中任意目标（玩家/方块）均爆炸，无火焰；
	// 通过自定义 ExplosionDamageCalculator 让 maodie 免受自己纸卷爆炸的伤害
	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		if (!this.level().isClientSide()) {
			Entity owner = this.getOwner();
			this.level().explode(
				this,
				Explosion.getDefaultDamageSource(this.level(), this),
				new ExplosionDamageCalculator() {
					@Override
					public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
						if (isMaodieOwner(owner) && entity == owner) {
							return false;
						}
						return super.shouldDamageEntity(explosion, entity);
					}
				},
				this.getX(), this.getY(), this.getZ(),
				EXPLOSION_POWER,
				false,
				ExplosionInteraction.MOB
			);
			this.discard();
		}
	}
}
