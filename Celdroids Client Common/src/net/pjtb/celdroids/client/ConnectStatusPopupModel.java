package net.pjtb.celdroids.client;

import net.pjtb.celdroids.NioSession;
import net.pjtb.celdroids.NioSession.IncompleteNioSession;

public class ConnectStatusPopupModel {
	public boolean error;
	public String message;

	protected IncompleteNioSession state;

	protected void progress(String message) {
		this.message = message;
		error = false;
	}

	protected void failed(String message) {
		this.message = message;
		error = true;
	}

	public NioSession update(float tDelta) {
		if (state == null)
			return null;

		NioSession ses = state.update(tDelta);
		if (state.error != null) {
			failed(state.error);
			state = null;
		} else if (ses != null) {
			progress("Success!");
			state = null;
		}
		return ses;
	}

	public void swappedOut() {
		if (state != null)
			state.cancel();
	}
}
