package net.pjtb.celdroids.client.menu;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.pjtb.celdroids.Constants;
import net.pjtb.celdroids.client.Button;
import net.pjtb.celdroids.client.ConfirmPopupScene;
import net.pjtb.celdroids.client.Model;
import net.pjtb.celdroids.client.Scene;
import net.pjtb.celdroids.client.menu.directconnect.DirectConnectSelectionScene;
import net.pjtb.celdroids.client.menu.lobby.AwaitingClientScene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MainMenuScene implements Scene {
	public enum MainMenuSubSceneType { HOST, DIRECT_IP_CONNECT, P2P_CONNECT, CONFIRM_FLEE_POPUP }

	private final Model model;

	protected final Map<MainMenuSubSceneType, Scene> subScenes;
	protected Scene subScene;

	protected final List<Button> buttons;

	public MainMenuScene(Model m) {
		this.model = m;

		subScenes = new EnumMap<MainMenuSubSceneType, Scene>(MainMenuSubSceneType.class);
		subScenes.put(MainMenuSubSceneType.HOST, new AwaitingClientScene(m, this));
		subScenes.put(MainMenuSubSceneType.DIRECT_IP_CONNECT, new DirectConnectSelectionScene(m, this));
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
		buttons.add(new Button(m, "Host", new Runnable() {
			@Override
			public void run() {
				subScene = subScenes.get(MainMenuSubSceneType.HOST);
				subScene.swappedIn(true);
			}
		}, 310, 10, 256, 128));
		buttons.add(new Button(m, "Connect", new Runnable() {
			@Override
			public void run() {
				subScene = subScenes.get(MainMenuSubSceneType.DIRECT_IP_CONNECT);
				subScene.swappedIn(true);
			}
		}, 610, 10, 256, 128));
		buttons.add(new Button(m, null, new Runnable() {
			@Override
			public void run() {
				close();
			}
		}, 1172, 576, 108, 144, "ui/menuScene/close", "ui/menuScene/selectedClose", 255, 255, 255, 255, -1, -1, -1, -1));
	}

	private void close() {
		subScene = subScenes.get(MainMenuSubSceneType.CONFIRM_FLEE_POPUP);
		subScene.swappedIn(true);
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
				close();
			} else if (model.controller.wasMenuPressed && !Gdx.input.isKeyPressed(Keys.ENTER) && !Gdx.input.isKeyPressed(Keys.MENU)) {
				
			}
		} else {
			subScene.update(tDelta);
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		Texture image = model.assets.get("images/backgrounds/titleScreen.png", Texture.class);
		batch.draw(image, 0, Constants.HEIGHT - image.getHeight());

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
