package functionhook.oldwu.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import functionhook.oldwu.effect.EternalRemoval;
import functionhook.oldwu.effect.ModEffects;

/**
 * 春秋肠：食用后解除"永恒"状态效果（作为永恒的后门解除手段），
 * 并清零野生狗奶计数器；若玩家位于宇宙热寂维度，还会将其传送回主世界。
 * 仍保留原有的食物/中毒/反胃等消耗效果（由 CONSUMABLE 组件处理）。
 */
public class ChunqiuChangItem extends Item {
	public ChunqiuChangItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
		ItemStack result = super.finishUsingItem(stack, level, livingEntity);
		if (!level.isClientSide()) {
			EternalRemoval.run(() -> livingEntity.removeEffect(ModEffects.ETERNAL));
			if (livingEntity instanceof ServerPlayer player) {
				GounaiDrinkTracker.onChunqiuChang(player);
			}
		}
		return result;
	}
}
