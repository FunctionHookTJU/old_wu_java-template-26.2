package functionhook.oldwu.client.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.feline.Cat;

import functionhook.oldwu.cat.GoodCatLogic;

/**
 * 猫信息 HUD：当玩家准星看向一只猫时，在屏幕中央偏左显示：
 * <ul>
 *   <li>好猫值及类型（键帽/坏猫/普通猫/绝世好猫）</li>
 *   <li>血量上限</li>
 *   <li>老吴撼地掌攻击伤害 ({@code (100-好猫值)*0.1})</li>
 *   <li>恐吓类 / 攻击类概率（恐吓 = 好猫值%，攻击 = 100-好猫值%）</li>
 * </ul>
 *
 * <p>跟随准星目标逐帧计算，视角离开猫时立即消失。数据均来自同步到客户端的
 * {@code SynchedEntityData}，无需额外发包。
 */
public final class CatInfoHud {
	/** 面板右边缘距屏幕中心的距离。 */
	private static final int CENTER_OFFSET = 96;
	private static final int PADDING = 4;
	private static final int LINE_HEIGHT = 10;
	private static final int TEXT_COLOR = 0xFFFFFFFF;
	private static final int BACKGROUND_COLOR = 0xA0000000;

	private CatInfoHud() {
	}

	public static void extract(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.crosshairPickEntity == null) {
			return;
		}
		Entity target = mc.crosshairPickEntity;
		if (!(target instanceof Cat cat)) {
			return;
		}

		int value = Mth.clamp(GoodCatLogic.getGoodValue(cat), 0, GoodCatLogic.MAX_VALUE);

		List<String> lines = new ArrayList<>();
		lines.add("好猫值: " + value + " (" + typeName(value) + ")");
		lines.add("血量上限: " + Mth.floor(cat.getMaxHealth()));
		lines.add(String.format("撼地掌伤害: %.1f", GoodCatLogic.palmDamage(cat)));
		// 已驯服猫恐吓固定 10%（攻击 90%）；未驯服按好猫值
		int intimidate = cat.isTame()
			? Math.round(GoodCatLogic.TAMED_INTIMIDATE_CHANCE * 100)
			: value;
		lines.add(String.format("恐吓: %d%% / 攻击: %d%%", intimidate, 100 - intimidate));

		int panelW = 0;
		for (String line : lines) {
			panelW = Math.max(panelW, mc.font.width(line));
		}
		panelW += PADDING * 2;
		int panelH = lines.size() * LINE_HEIGHT + PADDING * 2;

		int centerX = graphics.guiWidth() / 2;
		int centerY = graphics.guiHeight() / 2;
		int panelX = centerX - CENTER_OFFSET - panelW;
		int panelY = centerY - panelH / 2;

		graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, BACKGROUND_COLOR);
		for (int i = 0; i < lines.size(); i++) {
			graphics.text(mc.font, lines.get(i), panelX + PADDING, panelY + PADDING + i * LINE_HEIGHT, TEXT_COLOR);
		}
	}

	private static String typeName(int value) {
		if (value < GoodCatLogic.KEYCAP_THRESHOLD) {
			return "键帽";
		}
		if (value < GoodCatLogic.BAD_THRESHOLD) {
			return "坏猫";
		}
		if (value < GoodCatLogic.NORMAL_THRESHOLD) {
			return "普通猫";
		}
		return "绝世好猫";
	}
}
