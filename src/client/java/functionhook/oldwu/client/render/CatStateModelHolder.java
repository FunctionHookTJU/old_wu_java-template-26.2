package functionhook.oldwu.client.render;

import functionhook.oldwu.client.model.*;

public interface CatStateModelHolder {
	MaodieCatModel oldwu_getMaodieModel();

	AngryCatModel oldwu_getAngryModel();

	AngryCatBabyModel oldwu_getAngryBabyModel();

	BattleCatModel oldwu_getBattleModel();

	BattleCatBabyModel oldwu_getBattleBabyModel();

	RecoveryCatModel oldwu_getRecoveryModel();

	RecoveryCatBabyModel oldwu_getRecoveryBabyModel();

	FlatCatModel oldwu_getFlatModel();

	FlatCatBabyModel oldwu_getFlatBabyModel();

	GroomingCatModel oldwu_getGroomingModel();
}
