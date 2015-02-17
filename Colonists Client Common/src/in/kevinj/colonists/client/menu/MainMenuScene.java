package in.kevinj.colonists.client.menu;

import in.kevinj.colonists.Constants;
import in.kevinj.colonists.client.Button;
import in.kevinj.colonists.client.ConfirmPopupScene;
import in.kevinj.colonists.client.Model;
import in.kevinj.colonists.client.PriorityQueueAssetManager;
import in.kevinj.colonists.client.PriorityQueueAssetManager.LoadEntry;
import in.kevinj.colonists.client.Scene;
import in.kevinj.colonists.client.menu.directconnect.DirectConnectSelectionScene;
import in.kevinj.colonists.client.menu.lobby.AwaitingClientScene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MainMenuScene implements Scene {
	public enum MainMenuSubSceneType {
		HOST, DIRECT_IP_CONNECT, P2P_CONNECT, CONFIRM_FLEE_POPUP
	}

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
				model.swapScene(model.scenes.get(Model.SceneType.WORLD));
			}
		}, 60, 450, 600, 128));
		buttons.add(new Button(m, "Host", new Runnable() {
			@Override
			public void run() {
				subScene = subScenes.get(MainMenuSubSceneType.HOST);
				subScene.swappedIn(true);
			}
		}, 60, 300, 600, 128));
		buttons.add(new Button(m, "Connect", new Runnable() {
			@Override
			public void run() {
				subScene = subScenes.get(MainMenuSubSceneType.DIRECT_IP_CONNECT);
				subScene.swappedIn(true);
			}
		}, 60, 150, 600, 128));
		buttons.add(new Button(m, null, new Runnable() {
			@Override
			public void run() {
				close();
			}
		}, 576, 1172, 144, 108, "closeButton/close", "closeButton/selectedClose", 255, 255, 255, 255, -1, -1, -1, -1));
	}

	@Override
	public Collection<LoadEntry> getAssetDependencies() {
		return Collections.singleton(new PriorityQueueAssetManager.LoadEntry("images/backgrounds/titleScreen.png", Texture.class, null));
	}

	@Override
	public Collection<String> getSpriteSheetDependencies() {
		return Collections.emptyList();
	}

	private void close() {
		subScene = subScenes.get(MainMenuSubSceneType.CONFIRM_FLEE_POPUP);
		subScene.swappedIn(true);
	}

	@Override
	public void swappedIn(boolean transition) {
		Gdx.gl20.glClearColor(0.5f, 0.5f, 0.5f, 1);
	}

	@Override
	public void resize(int width, int height) {
		
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
