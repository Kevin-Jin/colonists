package net.pjtb.celdroids.client.world;

import java.util.EnumMap;
import java.util.Map;

import net.pjtb.celdroids.client.Button;
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

	private final Button backButton, menuButton;

	public WorldScene(Model m) {
		this.model = new WorldModel(m);

		subScenes = new EnumMap<WorldSubSceneType, Scene>(WorldSubSceneType.class);
		subScenes.put(WorldSubSceneType.IN_GAME_MENU, new InGameMenuScene(m, this));

		backButton = new Button(m, null, new Runnable() {
			@Override
			public void run() {
				confirmBack();
			}
		}, 1172, 576, 108, 144, "ui/worldScene/back", "ui/worldScene/selectedBack", 255, 255, 255, 255);
		menuButton = new Button(m, null, new Runnable() {
			@Override
			public void run() {
				openPopupMenu();
			}
		}, 1172, 0, 108, 144, "ui/worldScene/more", "ui/worldScene/selectedMore", 255, 255, 255, 255);
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

	private void confirmBack() {
		//TODO: show a confirmation
		model.parent.scene.swappedOut(true);
		model.parent.scene = model.parent.scenes.get(Model.SceneType.MAIN_MENU);
		model.parent.scene.swappedIn(false);
	}

	private void openPopupMenu() {
		if (subScene != null) {
			subScene.swappedOut(false);
			subScene = null;
		} else {
			subScene = subScenes.get(WorldSubSceneType.IN_GAME_MENU);
			subScene.swappedIn(false);
		}
	}

	@Override
	public void update(float tDelta) {
		backButton.hidden = (subScene != null);
		backButton.update(tDelta);
		menuButton.hidden = (subScene != null);
		menuButton.update(tDelta);
		model.dpad.hidden = (subScene != null);
		model.dpad.update(tDelta);
		model.avatar.update(tDelta);
		model.updateActionButtonBehavior();
		if (model.actionButton.text != null)
			model.actionButton.update(tDelta);

		if (subScene == null) {
			if (model.parent.controller.wasBackPressed && !Gdx.input.isKeyPressed(Keys.ESCAPE) && !Gdx.input.isKeyPressed(Keys.BACK)) {
				confirmBack();
			} else if (model.parent.controller.wasMenuPressed && !Gdx.input.isKeyPressed(Keys.ENTER) && !Gdx.input.isKeyPressed(Keys.MENU)) {
				openPopupMenu();
			}
		} else {
			subScene.update(tDelta);
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		Sprite s = model.parent.sprites.get("environment/grass1");
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 12; j++) {
				s.setBounds(i * 60, j * 60, 60, 60);
				s.draw(batch);
			}
		}
		//draw map
		model.avatar.draw(batch);
		if (model.actionButton.text != null)
			model.actionButton.draw(batch);
		s = model.parent.sprites.get("ui/worldScene/controlBar");
		s.setBounds(960, 0, 320, 720);
		s.draw(batch);
		model.dpad.draw(batch);
		menuButton.draw(batch);
		backButton.draw(batch);
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
