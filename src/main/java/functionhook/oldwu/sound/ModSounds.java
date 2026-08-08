package functionhook.oldwu.sound;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

import functionhook.oldwu.Old_Wu_java;

/**
 * 自定义猫音效注册。文件名（去空格/括号）即为 sound event id：
 * {@code assets/old_wu_java/sounds/<id>.ogg}，见 {@code sounds.json}。
 */
public final class ModSounds {
	public static final SoundEvent HA_1 = registerSound("ha_1");
	public static final SoundEvent HA_2 = registerSound("ha_2");
	public static final SoundEvent HA_3 = registerSound("ha_3");
	public static final SoundEvent HA_4 = registerSound("ha_4");
	public static final SoundEvent HA_5 = registerSound("ha_5");
	public static final SoundEvent HA_6 = registerSound("ha_6");
	public static final SoundEvent HA_7 = registerSound("ha_7");
	public static final SoundEvent HA_8 = registerSound("ha_8");

	public static final SoundEvent LAOWU_1_1 = registerSound("laowu_1_1");
	public static final SoundEvent LAOWU_1_2 = registerSound("laowu_1_2");
	public static final SoundEvent LAOWU_1_3 = registerSound("laowu_1_3");
	public static final SoundEvent LAOWU_1_4 = registerSound("laowu_1_4");
	public static final SoundEvent LAOWU_1_5 = registerSound("laowu_1_5");
	public static final SoundEvent LAOWU_2_1 = registerSound("laowu_2_1");
	public static final SoundEvent LAOWU_2_2 = registerSound("laowu_2_2");
	public static final SoundEvent LAOWU_2_3 = registerSound("laowu_2_3");
	public static final SoundEvent LAOWU_2_4 = registerSound("laowu_2_4");
	public static final SoundEvent LAOWU_2_5 = registerSound("laowu_2_5");
	public static final SoundEvent LAOWU_2_6 = registerSound("laowu_2_6");
	public static final SoundEvent LAOWU_2_7 = registerSound("laowu_2_7");
	public static final SoundEvent LAOWU_2_8 = registerSound("laowu_2_8");
	public static final SoundEvent LAOWU_2_9 = registerSound("laowu_2_9");
	public static final SoundEvent LAOWU_3_1 = registerSound("laowu_3_1");
	public static final SoundEvent LAOWU_3_2 = registerSound("laowu_3_2");
	public static final SoundEvent LAOWU_3_3 = registerSound("laowu_3_3");
	public static final SoundEvent LAOWU_3_4 = registerSound("laowu_3_4");
	public static final SoundEvent LAOWU_3_5 = registerSound("laowu_3_5");

	public static final SoundEvent RECOVERY_1 = registerSound("recovery_1");
	public static final SoundEvent RECOVERY_2 = registerSound("recovery_2");

	public static final SoundEvent DAGOU_1 = registerSound("dagou_1");
	public static final SoundEvent DAGOU_2 = registerSound("dagou_2");
	public static final SoundEvent DAGOU_3 = registerSound("dagou_3");
	public static final SoundEvent DAGOU_4 = registerSound("dagou_4");
	public static final SoundEvent DAGOU_5 = registerSound("dagou_5");
	public static final SoundEvent DAGOU_6 = registerSound("dagou_6");
	public static final SoundEvent DAGOU_7 = registerSound("dagou_7");
	public static final SoundEvent DAGOU_8 = registerSound("dagou_8");
	public static final SoundEvent DAGOU_9 = registerSound("dagou_9");
	public static final SoundEvent DAGOU_10 = registerSound("dagou_10");
	public static final SoundEvent DAGOU_11_RE = registerSound("dagou_11_re");
	public static final SoundEvent DOG_LAUNCH = registerSound("dog_launch");

	public static final SoundEvent[] LAOWU_SERIES = {
		LAOWU_1_1, LAOWU_1_2, LAOWU_1_3, LAOWU_1_4, LAOWU_1_5,
		LAOWU_2_1, LAOWU_2_2, LAOWU_2_3, LAOWU_2_4, LAOWU_2_5, LAOWU_2_6, LAOWU_2_7, LAOWU_2_8, LAOWU_2_9,
		LAOWU_3_1, LAOWU_3_2, LAOWU_3_3, LAOWU_3_4, LAOWU_3_5
	};
	public static final SoundEvent[] HA_SERIES = {
		HA_1, HA_2, HA_3, HA_4, HA_5, HA_6, HA_7, HA_8
	};
	public static final SoundEvent[] RECOVERY_SERIES = {
		RECOVERY_1, RECOVERY_2
	};
	public static final SoundEvent[] DAGOU_SERIES = {
		DAGOU_1, DAGOU_2, DAGOU_3, DAGOU_4, DAGOU_5, DAGOU_6, DAGOU_7, DAGOU_8,
		DAGOU_9, DAGOU_10, DAGOU_11_RE, DOG_LAUNCH
	};

	private ModSounds() {
	}

	/**
	 * 触发静态字段初始化（完成所有 SoundEvent 注册）。
	 */
	public static void initialize() {
		Old_Wu_java.LOGGER.info("Registering {} sounds", Old_Wu_java.MOD_ID);
	}

	private static SoundEvent registerSound(String id) {
		Identifier identifier = Old_Wu_java.id(id);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, identifier, SoundEvent.createVariableRangeEvent(identifier));
	}
}
