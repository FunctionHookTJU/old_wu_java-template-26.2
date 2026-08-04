package functionhook.oldwu.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

import functionhook.oldwu.entity.ModEntityTypes;
import functionhook.oldwu.entity.PaperRoll;

/**
 * 纸卷物品：长按蓄力，松开发射纸卷弹射物，速度与蓄力时长正相关。
 */
public class PaperRollItem extends Item {
	// 满蓄力所需 tick 数（20 tick = 1 秒）
	public static final int MAX_CHARGE_TICKS = 20;
	// 满蓄力时弹射物初速度（方块/ tick）
	public static final float MAX_SPEED = 2.5F;
	// 低于该蓄力比例不发射
	public static final float MIN_CHARGE = 0.1F;

	public PaperRollItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		player.startUsingItem(hand);
		return InteractionResult.CONSUME;
	}

	// 可无限蓄力（实际以 MAX_CHARGE_TICKS 封顶）
	@Override
	public int getUseDuration(ItemStack stack, LivingEntity user) {
		return 72000;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.BOW;
	}

	@Override
	public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
		if (!(entity instanceof Player player)) {
			return false;
		}

		int timeHeld = this.getUseDuration(stack, entity) - remainingTime;
		float charge = Math.min(timeHeld / (float) MAX_CHARGE_TICKS, 1.0F);
		if (charge < MIN_CHARGE) {
			return false;
		}

		float speed = charge * MAX_SPEED;
		if (level instanceof ServerLevel serverLevel) {
			PaperRoll roll = new PaperRoll(ModEntityTypes.PAPER_ROLL, serverLevel);
			roll.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
			roll.setOwner(player);
			roll.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, speed, 0.0F);
			serverLevel.addFreshEntity(roll);
		}

		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EGG_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
		return true;
	}
}
