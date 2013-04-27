package net.pjtb.celdroids.client;

import java.util.EnumMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;

import net.pjtb.celdroids.client.scenes.Scene;

public class Model {
	public enum SceneType { LOAD_SCREEN, MAIN_MENU, WORLD, BATTLE }

	private final Map<SceneType, Scene> scenes;
	private volatile Scene scene;
	private volatile Scene swappedInScene;

	public Model() {
		scenes = new EnumMap<SceneType, Scene>(SceneType.class);
	}

	protected SceneFactory createSceneFactory() {
		return new SceneFactory();
	}

	public void createScenes() {
		SceneFactory sceneFactory = createSceneFactory();

		scenes.put(SceneType.LOAD_SCREEN, sceneFactory.makeLoadingScene());
		scenes.put(SceneType.MAIN_MENU, sceneFactory.makeMainMenuScene());
		scenes.put(SceneType.WORLD, sceneFactory.makeWorldScene());
		scenes.put(SceneType.BATTLE, sceneFactory.makeBattleScene());
		scenes.putAll(sceneFactory.additionalScenes());

		scene = scenes.get(SceneType.LOAD_SCREEN);
	}

	public void loadResources() {
		//load stuff here
		scene = scenes.get(SceneType.MAIN_MENU);
		if (swappedInScene == scenes.get(SceneType.LOAD_SCREEN)) {
			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run() {
					scenes.get(SceneType.LOAD_SCREEN).swappedOut();
					scene.swappedIn();
				}
			});
			swappedInScene = null;
		}
	}

	public Scene getScene() {
		return scene;
	}

	public void created() {
		swappedInScene = scene;
		swappedInScene.swappedIn();
		if (swappedInScene == scenes.get(SceneType.MAIN_MENU))
			swappedInScene = null;
	}
}
