package functionhook.oldwu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import functionhook.oldwu.item.ModItems;

/**
 * 为已驯服的狼添加"大狗叫"喂食逻辑：
 * - 掉血时：回血 10 点并消耗物品。
 * - 满血时：每次喂食按 1、2、3... 递增提升血量上限（最多 64 次）。
 *
 * <p>修复：效果只在服务端执行（客户端仅返回 SUCCESS 触发挥臂与发包），
 * 避免客户端与服务端双重执行导致的状态错乱；不再继承 Wolf（无需访问 protected 成员）。
 */
@Mixin(Wolf.class)
public abstract class WolfMixin {
	private static final String FEED_COUNT_KEY = "oldwu_dagoujiao_feeds";
	private static final int MAX_FEEDS = 64;

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

		// 客户端：仅返回 SUCCESS 触发交互（挥臂+发包），实际效果由服务端执行
		if (wolf.level().isClientSide()) {
			cir.setReturnValue(InteractionResult.SUCCESS);
			return;
		}

		CustomData customData = wolf.get(DataComponents.CUSTOM_DATA);
		CompoundTag tag = customData.copyTag();

		// 掉血：回血 10 点
		if (wolf.getHealth() < wolf.getMaxHealth()) {
			wolf.heal(10.0F);
			playEatSound(wolf);
			consume(player, stack);
			cir.setReturnValue(InteractionResult.SUCCESS);
			return;
		}

		// 满血：按喂食次数递增提升血量上限（第 n 次 +n，最多 64 次）
		int feeds = tag.getInt(FEED_COUNT_KEY).orElse(0);
		if (feeds >= MAX_FEEDS) {
			cir.setReturnValue(InteractionResult.SUCCESS);
			return;
		}
		feeds += 1;
		int newFeeds = feeds;
		CustomData updated = customData.update(t -> t.putInt(FEED_COUNT_KEY, newFeeds));
		wolf.setComponent(DataComponents.CUSTOM_DATA, updated);

		float increase = feeds;
		AttributeInstance maxHealth = wolf.getAttribute(Attributes.MAX_HEALTH);
		if (maxHealth != null) {
			maxHealth.setBaseValue(maxHealth.getBaseValue() + increase);
		}
		wolf.heal(increase);
		playEatSound(wolf);
		consume(player, stack);
		cir.setReturnValue(InteractionResult.SUCCESS);
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
