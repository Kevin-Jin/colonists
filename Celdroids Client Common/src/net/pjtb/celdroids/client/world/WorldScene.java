package net.pjtb.celdroids.client.world;

import java.util.EnumMap;
import java.util.Map;

import net.pjtb.celdroids.client.Model;
import net.pjtb.celdroids.client.Scene;
import net.pjtb.celdroids.client.world.menu.InGameMenuScene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class WorldScene implements Scene {
	public enum WorldSubSceneType { IN_GAME_MENU }

	private final WorldModel model;

	protected final Map<WorldSubSceneType, Scene> subScenes;
	private Scene subScene;

	public WorldScene(Model model) {
		this.model = new WorldModel(model);

		subScenes = new EnumMap<WorldSubSceneType, Scene>(WorldSubSceneType.class);
		subScenes.put(WorldSubSceneType.IN_GAME_MENU, new InGameMenuScene(model, this));
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
		model.dpad.hidden = (subScene != null);
		model.dpad.update(tDelta);
		model.avatar.update(tDelta);

		if (subScene == null) {
			if (model.parent.controller.wasBackPressed && !Gdx.input.isKeyPressed(Keys.ESCAPE) && !Gdx.input.isKeyPressed(Keys.BACK)) {
				//TODO: show a confirmation
				model.parent.scene.swappedOut(true);
				model.parent.scene = model.parent.scenes.get(Model.SceneType.MAIN_MENU);
				model.parent.scene.swappedIn(false);
			} else if (model.parent.controller.wasMenuPressed && !Gdx.input.isKeyPressed(Keys.ENTER) && !Gdx.input.isKeyPressed(Keys.MENU)) {
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
	public void draw(SpriteBatch batch) {
		Sprite s = model.parent.sprites.get("worldControls");
		s.setBounds(960, 0, 320, 720);
		s.draw(batch);
		model.dpad.draw(batch);
		model.avatar.draw(batch);
		if (subScene != null)
			subScene.draw(batch);
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
