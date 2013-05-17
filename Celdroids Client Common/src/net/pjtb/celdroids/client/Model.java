package net.pjtb.celdroids.client;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

public class Model {
	public enum SceneType { LOAD_SCREEN, MAIN_MENU, WORLD, BATTLE }

	public final ControllerHelper controller;

	public final Map<SceneType, Scene> scenes;
	public Scene scene;
	private Scene pausedScene;

	private boolean loading;
	private float remainingLoadTime;

	public final AssetManager assets;
	public final Map<String, Sprite> sprites;
	public SpriteBatch batch;

	public Model() {
		controller = new ControllerHelper();

		scenes = new EnumMap<SceneType, Scene>(SceneType.class);
		scene = EmptyScene.instance;
		assets = new AssetManager();
		sprites = new HashMap<String, Sprite>();
	}

	protected SceneFactory createSceneFactory() {
		return new SceneFactory();
	}

	public void onStart() {
		//initialize scene instances
		SceneFactory sceneFactory = createSceneFactory();
		scenes.put(SceneType.LOAD_SCREEN, sceneFactory.makeLoadingScene(this));
		scenes.put(SceneType.MAIN_MENU, sceneFactory.makeMainMenuScene(this));
		scenes.put(SceneType.WORLD, sceneFactory.makeWorldScene(this));
		scenes.put(SceneType.BATTLE, sceneFactory.makeBattleScene());
		scenes.putAll(sceneFactory.additionalScenes());
		scene = scenes.get(SceneType.LOAD_SCREEN);

		batch = new SpriteBatch();
	}

	public void startLoadingResources(float minSplashTime) {
		loading = true;
		remainingLoadTime = minSplashTime;

		//synchronous load loading screen assets
		TextureParameter param = new TextureParameter();
		param.minFilter = param.magFilter = TextureFilter.Nearest;
		assets.load("images/backgrounds/splash.png", Texture.class, param);
		assets.finishLoading();

		//asynchronous load all other assets
		assets.load("images/sprites/sprites.pack", TextureAtlas.class);
		assets.load("fonts/buttons.fnt", BitmapFont.class);
		//TODO: load music using assets.load(..., Music.class),
		//and sound effects using assets.load(..., Sound.class)
	}

	public void continueLoadingResources(float tDelta) {
		if (loading) {
			remainingLoadTime -= tDelta;
			if (assets.update()) {
				for (AtlasRegion reg : assets.get("images/sprites/sprites.pack", TextureAtlas.class).getRegions())
					sprites.put(reg.name, new Sprite(reg));

				loading = false;
				if (remainingLoadTime <= 0)
					finishedLoadingResources();
			}
		} else if (remainingLoadTime > 0) {
			remainingLoadTime -= tDelta;
			if (remainingLoadTime <= 0)
				finishedLoadingResources();
		}
	}

	private void finishedLoadingResources() {
		scene.swappedOut(true);
		if (pausedScene != null) {
			scene = pausedScene;
			scene.swappedIn(false);
			scene.resume();
			pausedScene = null;
		} else {
			scene = scenes.get(SceneType.MAIN_MENU);
			scene.swappedIn(false);
		}
	}

	public void onPause() {
		scene.pause();
		if (scene != scenes.get(SceneType.LOAD_SCREEN))
			pausedScene = scene;
	}

	public void onResume() {
		scene.swappedOut(false);
		scene = scenes.get(SceneType.LOAD_SCREEN);
		scene.swappedIn(false);
	}

	public void releaseAllResources() {
		assets.clear();
		sprites.clear();
	}

	public void onDispose() {
		assets.dispose();
	}
}
