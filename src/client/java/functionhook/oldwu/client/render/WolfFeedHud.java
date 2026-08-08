package functionhook.oldwu.client.render;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;

import functionhook.oldwu.attribute.ModAttributes;
import functionhook.oldwu.item.ModItems;

/**
 * 大狗叫喂食 HUD：当玩家背包中持有大狗叫并看向（准星瞄准）一只**属于自己的**已驯服狼时，
 * 在屏幕中央偏左绘制两条纵向条：
 * <ul>
 *   <li>绿色蓄力条（左侧，共 12 格）：喂满 64 次后每次蓄力 +1 格（自动充能）。</li>
 *   <li>黄色喂食进度条（右侧）：进度 = 喂食次数 / 64，满 64 次填满。</li>
 * </ul>
 *
 * <p>进度条跟随准星目标逐帧计算，视角离开狼时立即消失。
 * 填充色 {@code #E5A822}，蓄力色 {@code #55FF55}，边框与底色 {@code #1E1F22}。
 *
 * <p>只能喂食属于自己的狼（{@code isOwnedBy}），进度条同样只对主人显示。
 *
 * <p>喂食次数由同步到客户端的 {@code extra_max_health} 属性反推：
 * 第 n 次喂食增加 n，累计 {@code n(n+1)/2}，解方程可得 n。
 * 蓄力格数直接读取同步属性 {@code charge}。
 * 实体 {@code CustomData} 不随 {@code setComponent} 同步到客户端，不能作为进度依据。
 */
public final class WolfFeedHud {
	public static final int MAX_FEEDS = 64;
	public static final int MAX_CHARGE = 12;

	private static final int BAR_WIDTH = 8;
	private static final int BAR_HEIGHT = 72;
	private static final int CENTER_OFFSET = 48;
	private static final int BAR_GAP = 8;
	private static final int FILL_COLOR = 0xFFE5A822;
	private static final int CHARGE_COLOR = 0xFF55FF55;
	private static final int FRAME_COLOR = 0xFF1E1F22;

	private WolfFeedHud() {
	}

	public static void extract(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.crosshairPickEntity == null) {
			return;
		}
		if (!hasDagoujiao(mc.player)) {
			return;
		}
		Entity target = mc.crosshairPickEntity;
		if (!(target instanceof Wolf wolf) || !wolf.isTame() || !wolf.isOwnedBy(mc.player)) {
			return;
		}

		int feeds = readFeedCount(wolf);
		float progress = Mth.clamp(feeds / (float) MAX_FEEDS, 0.0F, 1.0F);
		int charge = Mth.clamp(readCharge(wolf), 0, MAX_CHARGE);

		int centerX = graphics.guiWidth() / 2;
		int centerY = graphics.guiHeight() / 2;
		int barY = centerY - BAR_HEIGHT / 2;

		// 绿色蓄力条（最左侧，13 格）
		drawChargeBar(graphics, centerX - CENTER_OFFSET - BAR_WIDTH - BAR_GAP, barY, charge);

		// 黄色喂食进度条（右侧）
		drawFeedBar(graphics, centerX - CENTER_OFFSET, barY, progress);
	}

	private static void drawFeedBar(GuiGraphicsExtractor graphics, int barX, int barY, float progress) {
		graphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, FRAME_COLOR);

		int innerX = barX + 1;
		int innerY = barY + 1;
		int innerW = BAR_WIDTH - 2;
		int innerH = BAR_HEIGHT - 2;
		int fillHeight = Math.round(innerH * progress);
		if (fillHeight > 0) {
			graphics.fill(innerX, innerY + innerH - fillHeight, innerX + innerW, innerY + innerH, FILL_COLOR);
		}
	}

	private static void drawChargeBar(GuiGraphicsExtractor graphics, int barX, int barY, int charge) {
		graphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, FRAME_COLOR);

		int innerX = barX + 1;
		int innerY = barY + 1;
		int innerW = BAR_WIDTH - 2;
		int innerH = BAR_HEIGHT - 2;
		float segHF = innerH / (float) MAX_CHARGE;
		for (int k = 1; k <= charge; k++) {
			int yBot = innerY + Math.round(innerH - (k - 1) * segHF);
			int yTop = innerY + Math.round(innerH - k * segHF) + 1;
			if (yTop < yBot) {
				graphics.fill(innerX, yTop, innerX + innerW, yBot, CHARGE_COLOR);
			}
		}
	}

	/**
	 * 是否"拥有"大狗叫：手持（主/副手）或背包任意位置存在即可（充能为背包自动消耗机制）。
	 */
	private static boolean hasDagoujiao(Player player) {
		return player.getMainHandItem().is(ModItems.DAGOUJIAO)
			|| player.getOffhandItem().is(ModItems.DAGOUJIAO)
			|| player.getInventory().hasAnyMatching(s -> s.is(ModItems.DAGOUJIAO));
	}

	private static int readFeedCount(Wolf wolf) {
		AttributeInstance extra = wolf.getAttribute(ModAttributes.EXTRA_MAX_HEALTH);
		if (extra == null) {
			return 0;
		}
		double bonus = extra.getValue();
		long feeds = Math.round((Math.sqrt(8.0 * bonus + 1.0) - 1.0) / 2.0);
		if (feeds < 0) {
			return 0;
		}
		return (int) Math.min(feeds, MAX_FEEDS);
	}

	private static int readCharge(Wolf wolf) {
		AttributeInstance charge = wolf.getAttribute(ModAttributes.CHARGE);
		return charge == null ? 0 : (int) charge.getValue();
	}
}
