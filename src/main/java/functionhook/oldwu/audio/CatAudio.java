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
 * 同一只猫同一状态的两次播放间隔不小于该状态对应的 {@link #minIntervalTicks}，
 * 保证上一段音频基本播完再播下一段、不重叠。
 */
public final class CatAudio {
	private static final int MIN_ANGRY_INTERVAL_TICKS = 20;
	private static final int MIN_PAIRING_INTERVAL_TICKS = 100;
	private static final int MIN_BATTLE_INTERVAL_TICKS = 20;
	private static final int MIN_RECOVERY_INTERVAL_TICKS = 40;
	private static final Map<UUID, Map<CatState, Integer>> LAST_PLAY_TICK = new HashMap<>();

	private CatAudio() {
	}

	public static void playStateSound(Cat cat, CatState state) {
		playSound(cat, pickStateSound(cat, state), state);
	}

	/**
	 * maodie 攻击/发射纸筒时播放 ha 系列音效（带字幕，兼容原版字幕显示）。
	 */
	public static void playHaSound(Cat cat) {
		playSound(cat, pickRandom(cat, ModSounds.HA_SERIES), null);
	}

	private static void playSound(Cat cat, SoundEvent sound, CatState state) {
		if (sound == null) {
			return;
		}

		Map<CatState, Integer> lastTicks = LAST_PLAY_TICK.computeIfAbsent(cat.getUUID(), key -> new HashMap<>());
		Integer last = lastTicks.get(state);
		if (last != null && cat.tickCount - last < minIntervalTicks(state)) {
			return;
		}
		lastTicks.put(state, cat.tickCount);

		// 以猫实体为声源播放，声音随猫移动并按其位置衰减
		cat.level().playSound(null, cat, sound, cat.getSoundSource(), 1.0F, 1.0F);
	}

	private static int minIntervalTicks(CatState state) {
		return switch (state) {
			case PAIRING -> MIN_PAIRING_INTERVAL_TICKS;
			case BATTLE -> MIN_BATTLE_INTERVAL_TICKS;
			case RECOVERY -> MIN_RECOVERY_INTERVAL_TICKS;
			// ha 系列（playHaSound 传入 null）：沿用普通间隔
			case null -> MIN_ANGRY_INTERVAL_TICKS;
			default -> MIN_ANGRY_INTERVAL_TICKS;
		};
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
