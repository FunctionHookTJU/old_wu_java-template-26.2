package functionhook.oldwu.client.mixin;

import net.minecraft.client.renderer.entity.state.CatRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import functionhook.oldwu.client.render.CatStateCarrier;

@Mixin(CatRenderState.class)
public abstract class CatRenderStateMixin implements CatStateCarrier {
	@Unique
	private int oldwuStateId;
	@Unique
	private boolean oldwuMaodie;
	@Unique
	private int oldwuDanceModelIndex;

	@Override
	public int oldwu_getStateId() {
		return this.oldwuStateId;
	}

	@Override
	public void oldwu_setStateId(int stateId) {
		this.oldwuStateId = stateId;
	}

	@Override
	public boolean oldwu_isMaodie() {
		return this.oldwuMaodie;
	}

	@Override
	public void oldwu_setMaodie(boolean maodie) {
		this.oldwuMaodie = maodie;
	}

	@Override
	public int oldwu_getDanceModelIndex() {
		return this.oldwuDanceModelIndex;
	}

	@Override
	public void oldwu_setDanceModelIndex(int index) {
		this.oldwuDanceModelIndex = index;
	}
}
