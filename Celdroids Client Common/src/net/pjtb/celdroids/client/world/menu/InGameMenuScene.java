package net.pjtb.celdroids.client.world.menu;

import net.pjtb.celdroids.client.Model;
import net.pjtb.celdroids.client.Scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class InGameMenuScene implements Scene {
	private final Model model;

	private final Scene parentScene;

	public InGameMenuScene(Model model, Scene worldScene) {
		this.model = model;

		this.parentScene = worldScene;
	}

	@Override
	public void swappedIn(boolean transition) {
		
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void update(float tDelta) {
		if (model.controller.wasBackPressed && !Gdx.input.isKeyPressed(Keys.ESCAPE) && !Gdx.input.isKeyPressed(Keys.BACK)) {
			swappedOut(true);
			parentScene.setSubscene(null);
		} else if (model.controller.wasMenuPressed && !Gdx.input.isKeyPressed(Keys.ENTER) && !Gdx.input.isKeyPressed(Keys.MENU)) {
			swappedOut(true);
			parentScene.setSubscene(null);
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		
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
