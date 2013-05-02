package net.pjtb.celdroids.client;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import net.pjtb.celdroids.client.scenes.EmptyScene;
import net.pjtb.celdroids.client.scenes.Scene;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

public class Model {
	public enum SceneType { LOAD_SCREEN, MAIN_MENU, WORLD, BATTLE }

	private final Map<SceneType, Scene> scenes;
	private Scene scene, pausedScene;

	private boolean loading;
	private float remainingLoadTime;

	public final AssetManager assets;
	public final Map<String, Sprite> sprites;
	public SpriteBatch batch;

	public Model() {
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
		scenes.put(SceneType.MAIN_MENU, sceneFactory.makeMainMenuScene());
		scenes.put(SceneType.WORLD, sceneFactory.makeWorldScene());
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
		//TODO: load music using assets.load(..., Music.class),
		//sound effects using assets.load(..., Sound.class), and
		//fonts using assets.load(..., BitmapFont.class)
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
		scene.swappedOut();
		if (pausedScene != null) {
			scene = pausedScene;
			scene.swappedIn();
			scene.resume();
			pausedScene = null;
		} else {
			scene = scenes.get(SceneType.MAIN_MENU);
			scene.swappedIn();
		}
	}

	public void onPause() {
		scene.pause();
		if (scene != scenes.get(SceneType.LOAD_SCREEN))
			pausedScene = scene;
	}

	public void onResume() {
		scene.swappedOut();
		scene = scenes.get(SceneType.LOAD_SCREEN);
		scene.swappedIn();
	}

	public void releaseAllResources() {
		assets.clear();
		sprites.clear();
	}

	public Scene getScene() {
		return scene;
	}

	public void onDispose() {
		assets.dispose();
	}
}
