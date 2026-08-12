package functionhook.oldwu.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import functionhook.oldwu.Old_Wu_java;
import functionhook.oldwu.effect.ModEffects;

/**
 * 永恒（Eternal）效果客户端渲染：
 * 当本地玩家拥有永恒效果时，将血条的心替换为 {@code hud/heart/infinite} 贴图。
 *
 * <p>渲染机制参考原版中毒效果（{@code HeartType.forPlayer}）：只有玩家自己
 * 的实心心（NORMAL/中毒/凋零/冰冻/吸收）会被替换，而 CONTAINER（空槽背景心）
 * 保持原样，因此不会出现"其他生物的心也被替换"的问题。
 */
@Mixin(targets = "net.minecraft.client.gui.Hud$HeartType")
public abstract class HudHeartMixin {
	private static final Identifier INFINITE_HEART = Old_Wu_java.id("hud/heart/infinite");

	@Inject(method = "getSprite", at = @At("HEAD"), cancellable = true)
	private void oldwu_eternalHeartSprite(boolean hardcore, boolean half, boolean blinking, CallbackInfoReturnable<Identifier> cir) {
		Player player = Minecraft.getInstance().player;
		if (player == null || !player.hasEffect(ModEffects.ETERNAL)) {
			return;
		}
		// 参考中毒效果：只替换玩家自己的实心心，CONTAINER 空槽背景保持原样。
		if ("CONTAINER".equals(((Enum<?>) (Object) this).name())) {
			return;
		}
		cir.setReturnValue(INFINITE_HEART);
	}
}
