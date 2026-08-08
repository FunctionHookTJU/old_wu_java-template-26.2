package functionhook.oldwu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;

import functionhook.oldwu.attribute.ModAttributes;
import functionhook.oldwu.item.ModItems;
import functionhook.oldwu.sound.ModSounds;

/**
 * 为已驯服的狼添加"大狗叫"逻辑：
 * <ul>
 *   <li>喂食（右键，需手持大狗叫）：掉血时回血 10 点；满血且喂食次数未满 64 时，
 *       每次按 1、2、3... 递增提升血量上限并回复等量生命。</li>
 *   <li>满 64 次后：获得永久力量 V + 生命恢复 III，并开启蓄力充能。</li>
 *   <li>充能（自动）：**仅当狼存在攻击目标时**，每 tick 检测主人背包中是否有大狗叫，
 *       存在则自动消耗 1 个并蓄力 +1（共 12 格），依序播放 大狗1~10、大狗11_re、dog_launch
 *       音频；**血量不满时也能充能**。</li>
 *   <li>充能冷却：需等上一段蓄力音频播放结束才能蓄下一次（按音频时长计 tick）。</li>
 *   <li>第 12 次蓄力：清空蓄力条并触发与监守者（warden）相同的音波攻击（伤害更高），
 *       音波攻击在 dog_launch 音频**开始后 1 tick**释放（不等其播完）。</li>
 * </ul>
 *
 * <p>修复：喂食效果只在服务端执行（客户端仅返回 SUCCESS 触发挥臂与发包），
 * 避免客户端与服务端双重执行导致的状态错乱；不再继承 Wolf（无需访问 protected 成员）。
 *
 * <p>血量上限提升改用无上限的自定义属性 {@code extra_max_health}（见
 * {@link ModAttributes}），因为原版 {@code generic.max_health} 有 1024 上限，
 * 无法支撑喂食 64 次所需的最大生命值；叠加逻辑见 {@code LivingEntity#getMaxHealth}。
 *
 * <p>蓄力进度存于同步属性 {@code charge}，供客户端 HUD 绿色蓄力条读取。
 */
@Mixin(Wolf.class)
public abstract class WolfMixin {
	private static final String FEED_COUNT_KEY = "oldwu_dagoujiao_feeds";
	private static final int MAX_FEEDS = 64;
	private static final int MAX_CHARGE = 12;
	private static final int PERMANENT_DURATION = Integer.MAX_VALUE;

	/** 音波攻击伤害（监守者为 10，这里更高）。 */
	private static final float SONIC_BOOM_DAMAGE = 67.0F;
	/** 音波攻击目标搜索范围（当狼没有仇恨目标时）。 */
	private static final double SONIC_RANGE = 16.0D;
	/** 自动充能：主人距狼的最大距离（方块）。 */
	private static final double AUTO_CHARGE_RANGE = 16.0D;
	/** 各段蓄力音频的时长（tick）。第 1~11 段由音频实测换算（秒×20 向上取整）；
	 *  第 12 段（dog_launch）只需 1 tick——音波攻击在其开始后 1 tick 释放。 */
	private static final int[] CHARGE_AUDIO_TICKS = {31, 29, 27, 26, 25, 24, 23, 22, 21, 20, 18, 1};

	/** 蓄力音频最早可喂下一口的游戏时间（仅服务端使用）。 */
	@Unique
	private long chargeSoundReadyAt;

	/**
	 * 自动充能：每 tick 检查主人背包中的大狗叫，存在则自动消耗并蓄力（无视血量）。
	 */
	@Inject(method = "tick", at = @At("TAIL"))
	private void oldwu_autoChargeTick(CallbackInfo ci) {
		if (!((Wolf) (Object) this).level().isClientSide()) {
			autoCharge();
		}
	}

	@Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
	private void oldwu_feedDagoujiao(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		ItemStack stack = player.getItemInHand(hand);
		if (!stack.is(ModItems.DAGOUJIAO)) {
			return;
		}
		Wolf wolf = (Wolf) (Object) this;
		if (!wolf.isTame()) {
			return;
		}
		// 只能喂食属于自己的狼；非主人的玩家（双端一致）直接放行，不触发挥臂也不产生效果
		if (!wolf.isOwnedBy(player)) {
			cir.setReturnValue(InteractionResult.PASS);
			return;
		}

		// 客户端：仅返回 SUCCESS 触发交互（挥臂+发包），实际效果由服务端执行
		if (wolf.level().isClientSide()) {
			cir.setReturnValue(InteractionResult.SUCCESS);
			return;
		}

		CustomData customData = wolf.get(DataComponents.CUSTOM_DATA);
		CompoundTag tag = customData.copyTag();
		int feeds = tag.getInt(FEED_COUNT_KEY).orElse(0);

		// 掉血：回血 10 点
		if (wolf.getHealth() < wolf.getMaxHealth()) {
			wolf.heal(10.0F);
			playEatSound(wolf);
			consume(player, stack);
			cir.setReturnValue(InteractionResult.SUCCESS);
			return;
		}

		// 满血且喂食进度未满：第 n 次喂食血量上限 +n 并回复等量生命
		if (feeds < MAX_FEEDS) {
			feeds += 1;
			float increase = feeds;
			AttributeInstance extraHealth = wolf.getAttribute(ModAttributes.EXTRA_MAX_HEALTH);
			if (extraHealth != null) {
				extraHealth.setBaseValue(extraHealth.getBaseValue() + increase);
			}
			wolf.heal(increase);
			playEatSound(wolf);
			if (feeds >= MAX_FEEDS) {
				grantPermanentEffects(wolf);
			}
			int newFeeds = feeds;
			CustomData updated = customData.update(t -> t.putInt(FEED_COUNT_KEY, newFeeds));
			wolf.setComponent(DataComponents.CUSTOM_DATA, updated);
			consume(player, stack);
			cir.setReturnValue(InteractionResult.SUCCESS);
			return;
		}

		// 已满 64 次：充能由背包自动消耗机制处理，此处无需手动喂食
		cir.setReturnValue(InteractionResult.SUCCESS);
	}

	/**
	 * 自动充能核心：仅当狼存在攻击目标时，才消耗主人背包中的大狗叫并蓄力 +1（无视血量）。
	 * 第 12 次蓄力（dog_launch 开始后 1 tick）释放音波攻击并清空蓄力条。
	 */
	private void autoCharge() {
		Wolf wolf = (Wolf) (Object) this;

		// 第 12 次蓄力：dog_launch 音频开始后 1 tick（冷却即 1 tick）释放音波攻击并清空蓄力条（不依赖主人位置/背包）
		int charge = readCharge(wolf);
		if (charge >= MAX_CHARGE) {
			if (wolf.level().getGameTime() >= this.chargeSoundReadyAt) {
				setCharge(wolf, 0);
				triggerSonicBoom(wolf);
			}
			return;
		}

		// 主人必须是玩家且在附近
		if (!(wolf.getOwner() instanceof Player owner)) {
			return;
		}
		if (wolf.distanceToSqr(owner) > AUTO_CHARGE_RANGE * AUTO_CHARGE_RANGE) {
			return;
		}
		// 只有存在攻击目标时才消耗大狗叫
		LivingEntity target = wolf.getTarget();
		if (target == null || !target.isAlive() || target.distanceToSqr(wolf) > 32.0D * 32.0D) {
			return;
		}
		// 背包中必须有大狗叫
		if (!owner.getInventory().hasAnyMatching(s -> s.is(ModItems.DAGOUJIAO))) {
			return;
		}
		// 充能系统需已激活（喂满 64 次）
		if (readFeeds(wolf) < MAX_FEEDS) {
			return;
		}
		// 需等上一段蓄力音频播放结束
		if (wolf.level().getGameTime() < this.chargeSoundReadyAt) {
			return;
		}

		// 自动消耗背包中的 1 个大狗叫
		consumeDagoujiaoFromInventory(owner);

		charge += 1;
		setCharge(wolf, charge);
		playChargeSound(wolf, charge);
		this.chargeSoundReadyAt = wolf.level().getGameTime() + chargeAudioTicks(charge);
		// 第 12 次蓄力不在此处释放音波攻击，1 tick 后由上方逻辑释放
		grantPermanentEffects(wolf);
	}

	private static void grantPermanentEffects(Wolf wolf) {
		wolf.addEffect(new MobEffectInstance(MobEffects.STRENGTH, PERMANENT_DURATION, 3, false, false, true));
		wolf.addEffect(new MobEffectInstance(MobEffects.REGENERATION, PERMANENT_DURATION, 2, false, false, true));
	}

	/**
	 * 音波攻击：与监守者（warden）的 sonic boom 一致——沿目标方向产生密集的 SONIC_BOOM 粒子、
	 * 播放 {@code WARDEN_SONIC_BOOM} 音效，并对目标造成 sonic_boom 伤害与击退。
	 */
	private static void triggerSonicBoom(Wolf wolf) {
		if (!(wolf.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		LivingEntity target = findSonicTarget(wolf);
		if (target == null) {
			return;
		}

		Vec3 start = wolf.getEyePosition();
		Vec3 dir = target.getEyePosition().subtract(start).normalize();
		double distance = start.distanceTo(target.getEyePosition());
		// 每 0.5 格生成 2 颗粒子并带轻微散布，粒子密度约为监守者的 4 倍
		double maxDistance = distance + 7.0D;
		for (double d = 0.5D; d < maxDistance; d += 0.5D) {
			Vec3 point = start.add(dir.scale(d));
			serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, point.x, point.y, point.z, 2, 0.1D, 0.1D, 0.1D, 0.0D);
		}
		wolf.playSound(SoundEvents.WARDEN_SONIC_BOOM, 3.0F, 1.0F);

		if (target.hurtServer(serverLevel, serverLevel.damageSources().sonicBoom(wolf), SONIC_BOOM_DAMAGE)) {
			double vertical = 0.5D * (1.0D - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
			double horizontal = 2.5D * (1.0D - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
			target.push(dir.x * horizontal, dir.y * vertical, dir.z * horizontal);
		}
	}

	/**
	 * 音波攻击目标：优先当前仇恨目标（32 格内）；否则取 16 格内最近的、
	 * 非自己/主人/其它已驯服狼的生物。
	 */
	private static LivingEntity findSonicTarget(Wolf wolf) {
		LivingEntity target = wolf.getTarget();
		if (target != null && target.isAlive() && target.distanceToSqr(wolf) < 32.0D * 32.0D) {
			return target;
		}
		LivingEntity best = null;
		double bestDistSq = Double.MAX_VALUE;
		for (LivingEntity entity : wolf.level().getEntitiesOfClass(LivingEntity.class, wolf.getBoundingBox().inflate(SONIC_RANGE), e ->
			e != wolf && e.isAlive() && !(e instanceof Wolf other && other.isTame()) && !e.equals(wolf.getOwner()))) {
			double distSq = entity.distanceToSqr(wolf);
			if (distSq < bestDistSq) {
				bestDistSq = distSq;
				best = entity;
			}
		}
		return best;
	}

	private static int readFeeds(Wolf wolf) {
		CustomData customData = wolf.get(DataComponents.CUSTOM_DATA);
		return customData.copyTag().getInt(FEED_COUNT_KEY).orElse(0);
	}

	private static int readCharge(Wolf wolf) {
		AttributeInstance charge = wolf.getAttribute(ModAttributes.CHARGE);
		return charge == null ? 0 : (int) charge.getValue();
	}

	private static void setCharge(Wolf wolf, int value) {
		AttributeInstance charge = wolf.getAttribute(ModAttributes.CHARGE);
		if (charge != null) {
			charge.setBaseValue(value);
		}
	}

	private static int chargeAudioTicks(int charge) {
		if (charge < 1 || charge > CHARGE_AUDIO_TICKS.length) {
			return 0;
		}
		return CHARGE_AUDIO_TICKS[charge - 1];
	}

	private static void consumeDagoujiaoFromInventory(Player player) {
		if (player.getAbilities().instabuild) {
			return;
		}
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (stack.is(ModItems.DAGOUJIAO)) {
				stack.shrink(1);
				return;
			}
		}
	}

	private static void playChargeSound(Wolf wolf, int charge) {
		if (charge < 1 || charge > ModSounds.DAGOU_SERIES.length) {
			return;
		}
		SoundEvent sound = ModSounds.DAGOU_SERIES[charge - 1];
		wolf.level().playSound(null, wolf, sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
	}

	private static void playEatSound(Wolf wolf) {
		wolf.level().playSound(wolf, wolf.blockPosition(), SoundEvents.GENERIC_EAT.value(), SoundSource.NEUTRAL, 1.0F, 1.0F);
	}

	private static void consume(Player player, ItemStack stack) {
		if (!player.getAbilities().instabuild) {
			stack.shrink(1);
		}
	}
}
