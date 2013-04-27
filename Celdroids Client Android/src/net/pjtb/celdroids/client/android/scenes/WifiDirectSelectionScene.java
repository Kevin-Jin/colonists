package net.pjtb.celdroids.client.android.scenes;

import net.pjtb.celdroids.client.android.WifiDirectModel;
import net.pjtb.celdroids.client.scenes.Scene;

public class WifiDirectSelectionScene implements Scene {
	private final WifiDirectModel model;

	public WifiDirectSelectionScene(WifiDirectModel model) {
		this.model = model;
	}

	@Override
	public void swappedIn() {
		model.swappedIn();
	}

	@Override
	public void pause() {
		model.pause();
	}

	@Override
	public void resume() {
		model.resume();
	}

	@Override
	public void update(float tDelta) {
		
	}

	@Override
	public void draw() {
		
	}

	@Override
	public void swappedOut() {
		
	}
}
