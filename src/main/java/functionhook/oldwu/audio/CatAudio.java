package functionhook.oldwu.audio;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.animal.feline.Cat;

import functionhook.oldwu.cat.CatState;
import functionhook.oldwu.sound.ModSounds;

/**
 * 状态音频播放：
 * <ul>
 *   <li>愤怒/配对：laowu 系列（随机）</li>
 *   <li>战斗：50% ha 系列、50% laowu 系列（随机）</li>
 *   <li>回血：recovery 系列（随机）</li>
 *   <li>普通/压扁：不播放模组音频（压扁还会通过 mixin 屏蔽原版音效）</li>
 * </ul>
 * 同一只猫两次播放间隔不小于 {@link #MIN_INTERVAL_TICKS}，避免声音重叠。
 */
public final class CatAudio {
	private static final int MIN_INTERVAL_TICKS = 20;
	private static final Map<UUID, Integer> LAST_PLAY_TICK = new HashMap<>();

	private CatAudio() {
	}

	public static void playStateSound(Cat cat, CatState state) {
		SoundEvent sound = pickStateSound(cat, state);
		if (sound == null) {
			return;
		}

		Integer last = LAST_PLAY_TICK.get(cat.getUUID());
		if (last != null && cat.tickCount - last < MIN_INTERVAL_TICKS) {
			return;
		}
		LAST_PLAY_TICK.put(cat.getUUID(), cat.tickCount);

		// 以猫实体为声源播放，声音随猫移动并按其位置衰减
		cat.level().playSound(null, cat, sound, cat.getSoundSource(), 1.0F, 1.0F);
	}

	private static SoundEvent pickStateSound(Cat cat, CatState state) {
		return switch (state) {
			case ANGRY, PAIRING -> pickRandom(cat, ModSounds.LAOWU_SERIES);
			case BATTLE -> pickRandom(cat, cat.getRandom().nextBoolean() ? ModSounds.HA_SERIES : ModSounds.LAOWU_SERIES);
			case RECOVERY -> pickRandom(cat, ModSounds.RECOVERY_SERIES);
			default -> null;
		};
	}

	private static SoundEvent pickRandom(Cat cat, SoundEvent[] series) {
		if (series == null || series.length == 0) {
			return null;
		}
		return series[cat.getRandom().nextInt(series.length)];
	}
}
