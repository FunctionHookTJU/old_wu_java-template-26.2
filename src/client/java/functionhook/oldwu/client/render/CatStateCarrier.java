package functionhook.oldwu.client.render;

public interface CatStateCarrier {
	int oldwu_getStateId();

	void oldwu_setStateId(int stateId);

	boolean oldwu_isMaodie();

	void oldwu_setMaodie(boolean maodie);

	int oldwu_getDanceModelIndex();

	void oldwu_setDanceModelIndex(int index);

	boolean oldwu_isMaodieHaqi();

	void oldwu_setMaodieHaqi(boolean haqi);

	int oldwu_getMaodieAnimTick();

	void oldwu_setMaodieAnimTick(int tick);

	boolean oldwu_isMaodieRage();

	void oldwu_setMaodieRage(boolean rage);

	int oldwu_getHitgroundAnimTick();

	void oldwu_setHitgroundAnimTick(int tick);
}
