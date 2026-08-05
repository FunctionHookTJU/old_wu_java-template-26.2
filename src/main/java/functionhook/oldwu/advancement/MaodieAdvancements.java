package functionhook.oldwu.advancement;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

/** Advancement rewards related to defeating maodie. */
public final class MaodieAdvancements {
	private static final Identifier DEFEAT_MAODIE_ID = Identifier.fromNamespaceAndPath("old_wu_java", "old_friend_gone");

	private MaodieAdvancements() {
	}

	public static void awardDefeatMaodie(ServerPlayer player) {
		ServerLevel level = (ServerLevel) player.level();
		AdvancementHolder advancement = level.getServer().getAdvancements().get(DEFEAT_MAODIE_ID);
		if (advancement != null) {
			player.getAdvancements().award(advancement, "defeat_maodie");
		}
	}
}