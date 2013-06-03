package net.pjtb.celdroids.client.menu;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.pjtb.celdroids.client.Button;
import net.pjtb.celdroids.client.ConfirmPopupScene;
import net.pjtb.celdroids.client.Model;
import net.pjtb.celdroids.client.Scene;
import net.pjtb.celdroids.client.menu.directconnect.DirectConnectSelectionScene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MainMenuScene implements Scene {
	public enum MainMenuSubSceneType { DIRECT_IP_CONNECT, P2P_CONNECT, CONFIRM_FLEE_POPUP }

	private final Model model;

	protected final Map<MainMenuSubSceneType, Scene> subScenes;
	protected Scene subScene;

	protected final List<Button> buttons;

	public MainMenuScene(Model m) {
		this.model = m;

		subScenes = new EnumMap<MainMenuSubSceneType, Scene>(MainMenuSubSceneType.class);
		subScenes.put(MainMenuSubSceneType.DIRECT_IP_CONNECT, new DirectConnectSelectionScene());
		subScenes.put(MainMenuSubSceneType.CONFIRM_FLEE_POPUP, new ConfirmPopupScene(m, "Are you sure you want to quit?", null));

		buttons = new ArrayList<Button>();
		buttons.add(new Button(m, "Local", new Runnable() {
			@Override
			public void run() {
				model.scene.swappedOut(true);
				model.scene = model.scenes.get(Model.SceneType.WORLD);
				model.scene.swappedIn(true);
			}
		}, 10, 10, 256, 128));
	}

	@Override
	public void swappedIn(boolean transition) {
		Gdx.gl10.glClearColor(0.5f, 0.5f, 0.5f, 1);
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
		boolean hidden = (subScene != null);
		for (Button button : buttons) {
			button.hidden = hidden;
			button.update(tDelta);
		}
		if (subScene == null) {
			if (model.controller.wasBackPressed && !Gdx.input.isKeyPressed(Keys.ESCAPE) && !Gdx.input.isKeyPressed(Keys.BACK)) {
				subScene = subScenes.get(MainMenuSubSceneType.CONFIRM_FLEE_POPUP);
				subScene.swappedIn(true);
			} else if (model.controller.wasMenuPressed && !Gdx.input.isKeyPressed(Keys.ENTER) && !Gdx.input.isKeyPressed(Keys.MENU)) {
				
			}
		} else {
			subScene.update(tDelta);
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		for (Button button : buttons)
			button.draw(batch);
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
