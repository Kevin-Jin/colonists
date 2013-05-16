package net.pjtb.celdroids.client.android.mainmenu.wifidirect;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

import net.pjtb.celdroids.client.Scene;

public class WifiDirectSelectionScene implements Scene {
	private final WifiDirectModel model;

	private final Scene parentScene;

	public WifiDirectSelectionScene(WifiDirectModel model, Scene mainMenuScene) {
		this.model = model;

		this.parentScene = mainMenuScene;
	}

	@Override
	public void swappedIn(boolean transition) {
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
		if (Gdx.input.isKeyPressed(Keys.ESCAPE) || Gdx.input.isKeyPressed(Keys.BACK)) {
			swappedOut(true);
			parentScene.setSubscene(null);
		} else if (Gdx.input.isKeyPressed(Keys.ENTER) || Gdx.input.isKeyPressed(Keys.MENU)) {
			
		}
	}

	@Override
	public void draw() {
		
	}

	@Override
	public void swappedOut(boolean transition) {
		
	}

	@Override
	public Scene getSubscene() {
		return null;
	}

	@Override
	public void setSubscene(Scene scene) {
		
	}
}
