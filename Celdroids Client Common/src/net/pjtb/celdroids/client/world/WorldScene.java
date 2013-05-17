package net.pjtb.celdroids.client.world;

import java.util.EnumMap;
import java.util.Map;

import net.pjtb.celdroids.client.Model;
import net.pjtb.celdroids.client.Scene;
import net.pjtb.celdroids.client.world.menu.InGameMenuScene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

public class WorldScene implements Scene {
	public enum WorldSubSceneType { IN_GAME_MENU }

	private final Model model;

	protected final Map<WorldSubSceneType, Scene> subScenes;
	private Scene subScene;

	public WorldScene(Model model) {
		this.model = model;

		subScenes = new EnumMap<WorldSubSceneType, Scene>(WorldSubSceneType.class);
		subScenes.put(WorldSubSceneType.IN_GAME_MENU, new InGameMenuScene());
	}

	@Override
	public void swappedIn(boolean transition) {
		Gdx.gl10.glClearColor(1f, 1f, 1f, 1);
	}

	@Override
	public void pause() {
		if (subScene != null)
			subScene.pause();
	}

	@Override
	public void resume() {
		if (subScene != null)
			subScene.resume();
	}

	@Override
	public void update(float tDelta) {
		//update our own scene stuff first, then subscene (if any)
		if (subScene == null) {
			if (model.controller.wasBackPressed && !Gdx.input.isKeyPressed(Keys.ESCAPE) && !Gdx.input.isKeyPressed(Keys.BACK)) {
				//TODO: show a confirmation
				model.scene.swappedOut(true);
				model.scene = model.scenes.get(Model.SceneType.MAIN_MENU);
				model.scene.swappedIn(false);
			} else if (model.controller.wasMenuPressed && !Gdx.input.isKeyPressed(Keys.ENTER) && !Gdx.input.isKeyPressed(Keys.MENU)) {
				if (subScene != null) {
					subScene.swappedOut(false);
					subScene = null;
				} else {
					subScene = subScenes.get(WorldSubSceneType.IN_GAME_MENU);
					subScene.swappedIn(false);
				}
			}
		} else {
			subScene.update(tDelta);
		}
	}

	@Override
	public void draw() {
		//draw our own scene stuff first, then overlay with subscene
		if (subScene != null)
			subScene.draw();
	}

	@Override
	public void swappedOut(boolean transition) {
		
	}

	@Override
	public Scene getSubscene() {
		return subScene;
	}

	@Override
	public void setSubscene(Scene scene) {
		this.subScene = scene;
	}
}
