package functionhook.oldwu.effect;

/**
 * 永恒效果的后门移除机制。
 *
 * <p>默认情况下 {@code EternalEffectMixin} 会拦截 {@code removeEffect} /
 * {@code removeEffectNoUpdate} / {@code removeAllEffects}，使永恒效果无法被牛奶、
 * 蜂蜜等解除。当需要"食用春秋肠清空永恒"时，通过 {@link #run} 在临时放行标志
 * 保护下执行移除逻辑即可。
 */
public final class EternalRemoval {
	/** 线程局部放行标志：{@code true} 时允许移除永恒效果。 */
	private static final ThreadLocal<Boolean> ALLOW_REMOVE = ThreadLocal.withInitial(() -> Boolean.FALSE);

	private EternalRemoval() {
	}

	public static boolean isAllowed() {
		return ALLOW_REMOVE.get();
	}

	/**
	 * 在临时放行永恒移除的上下文内执行 {@code action}（如移除效果），执行完毕后恢复拦截。
	 */
	public static void run(Runnable action) {
		ALLOW_REMOVE.set(Boolean.TRUE);
		try {
			action.run();
		} finally {
			ALLOW_REMOVE.remove();
		}
	}
}
