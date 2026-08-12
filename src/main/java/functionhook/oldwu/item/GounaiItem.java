package functionhook.oldwu.item;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;

import functionhook.oldwu.effect.ModEffects;

/**
 * 野生狗奶：药水类饮品，可饮用（DRINK 动画）。
 * 饮用后获得无限时长的"永恒"状态效果（无任何伤害、牛奶/蜂蜜无法解除）。
 * 每次饮用会推进隐藏计数器（见 {@link GounaiDrinkTracker}）：第 2 次"你将迈入永恒"并加速，
 * 第 3 次"『Made in Heaven』"超加速并永久保持，第 4 次还原刻速并传送至宇宙热寂虚空维度。
 * 第 3 次饮用后第 4 次饮用有 20 秒现实世界时间冷却（不随加速后的游戏刻走）。
 */
public class GounaiItem extends PotionItem {
	public GounaiItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		// 第 3 次饮用后的第 4 次饮用：按现实世界时间冷却，冷却期间无法开始饮用
		if (player instanceof ServerPlayer serverPlayer && GounaiDrinkTracker.isFourthDrinkOnCooldown(serverPlayer)) {
			long left = GounaiDrinkTracker.fourthDrinkCooldownSecondsLeft(serverPlayer);
			serverPlayer.sendOverlayMessage(Component.literal("野生狗奶冷却中，剩余 " + left + " 秒"));
			return InteractionResult.FAIL;
		}
		return super.use(level, player, hand);
	}

	@Override
	public ItemStack getDefaultInstance() {
		ItemStack stack = super.getDefaultInstance();
		// 奶白色自定义颜色（非水瓶），无效果
		stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.of(0xFFFFFF), List.of(), Optional.empty()));
		return stack;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
		ItemStack result = super.finishUsingItem(stack, level, livingEntity);
		if (!level.isClientSide()) {
			livingEntity.addEffect(new MobEffectInstance(ModEffects.ETERNAL, MobEffectInstance.INFINITE_DURATION, 0, false, true, true));
			if (livingEntity instanceof ServerPlayer player) {
				GounaiDrinkTracker.onDrink(player);
			}
		}
		return result;
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable(this.getDescriptionId());
	}
}
