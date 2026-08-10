package functionhook.oldwu.cat;

public enum CatState {
	COMMON,
	ANGRY,
	PAIRING,
	BATTLE,
	RECOVERY,
	FLAT,
	DANCE,
	GROOMING,
	HITGROUND;

	private static final CatState[] VALUES = values();

	public static CatState fromInt(int value) {
		return value >= 0 && value < VALUES.length ? VALUES[value] : COMMON;
	}
}
