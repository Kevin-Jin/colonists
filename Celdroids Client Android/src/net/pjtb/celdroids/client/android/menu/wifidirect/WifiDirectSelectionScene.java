package net.pjtb.celdroids.client.android.menu.wifidirect;

import net.pjtb.celdroids.client.Model;
import net.pjtb.celdroids.client.PlayerBattleOpponent;
import net.pjtb.celdroids.client.Scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

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
		PlayerBattleOpponent op = model.update(tDelta);
		if (op != null) {
			swappedOut(true);
			model.parent.scene.setSubscene(null);
			model.parent.scene.swappedOut(true);
			model.parent.scene = model.parent.scenes.get(Model.SceneType.BATTLE);
			model.parent.battleModel.initRemote(op);
			model.parent.scene.swappedIn(true);
			return;
		}

		if (model.parent.controller.wasBackPressed && !Gdx.input.isKeyPressed(Keys.ESCAPE) && !Gdx.input.isKeyPressed(Keys.BACK)) {
			swappedOut(true);
			parentScene.setSubscene(null);
		} else if (model.parent.controller.wasMenuPressed && !Gdx.input.isKeyPressed(Keys.ENTER) && !Gdx.input.isKeyPressed(Keys.MENU)) {
			
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		
	}

	@Override
	public void swappedOut(boolean transition) {
		if (transition)
			model.swappedOut();
	}

	@Override
	public Scene getSubscene() {
		return null;
	}

	@Override
	public void setSubscene(Scene scene) {
		
	}
}
