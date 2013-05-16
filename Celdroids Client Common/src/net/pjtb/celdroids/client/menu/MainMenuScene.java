package net.pjtb.celdroids.client.menu;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.pjtb.celdroids.client.Button;
import net.pjtb.celdroids.client.Model;
import net.pjtb.celdroids.client.Scene;
import net.pjtb.celdroids.client.menu.directconnect.DirectConnectSelectionScene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

public class MainMenuScene implements Scene {
	public enum MainMenuSubSceneType { ROOT, DIRECT_IP_CONNECT, P2P_CONNECT }

	protected final Map<MainMenuSubSceneType, Scene> subScenes;
	protected final List<Button> buttons;
	protected Scene subScene;

	public MainMenuScene(final Model m) {
		subScenes = new EnumMap<MainMenuSubSceneType, Scene>(MainMenuSubSceneType.class);
		subScenes.put(MainMenuSubSceneType.DIRECT_IP_CONNECT, new DirectConnectSelectionScene());

		buttons = new ArrayList<Button>();
		buttons.add(new Button(m, "Local", new Runnable() {
			@Override
			public void run() {
				m.scene.swappedOut(true);
				m.scene = m.scenes.get(Model.SceneType.WORLD);
				m.scene.swappedIn(false);
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
			if (Gdx.input.isKeyPressed(Keys.ESCAPE) || Gdx.input.isKeyPressed(Keys.BACK)) {
				//TODO: show a confirmation
				Gdx.app.exit();
			} else if (Gdx.input.isKeyPressed(Keys.ENTER) || Gdx.input.isKeyPressed(Keys.MENU)) {
				
			}
		} else {
			subScene.update(tDelta);
		}
	}

	@Override
	public void draw() {
		for (Button button : buttons)
			button.draw();
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
