package in.kevinj.colonists.client.world.menu;

import java.util.Collection;
import java.util.Collections;

import in.kevinj.colonists.client.Model;
import in.kevinj.colonists.client.Scene;
import in.kevinj.colonists.client.PriorityQueueAssetManager.LoadEntry;

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
	public Collection<LoadEntry> getAssetDependencies() {
		return Collections.emptyList();
	}

	@Override
	public Collection<String> getSpriteSheetDependencies() {
		return Collections.emptyList();
	}

	@Override
	public void swappedIn(boolean transition) {
		
	}

	@Override
	public void resize(int width, int height) {
		
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
