package functionhook.oldwu.item;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;

/**
 * 野生狗奶：药水类饮品，可饮用（DRINK 动画），暂不附带任何状态效果。
 */
public class GounaiItem extends PotionItem {
	public GounaiItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack getDefaultInstance() {
		ItemStack stack = super.getDefaultInstance();
		// 奶白色自定义颜色（非水瓶），无效果
		stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.of(0xFFFFFF), List.of(), Optional.empty()));
		return stack;
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable(this.getDescriptionId());
	}
}
