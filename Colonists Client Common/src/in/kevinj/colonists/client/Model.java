package in.kevinj.colonists.client;

import in.kevinj.colonists.Constants;
import in.kevinj.colonists.client.world.BattleModel;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

public class Model extends ScaleDisplay {
	public enum SceneType {
		LOAD_SCREEN, MAIN_MENU, WORLD, BATTLE
	}

	public final ControllerHelper controller;

	public final Map<SceneType, Scene> scenes;
	public Scene sceneToShow;
	private Scene currentScene;
	private boolean paused;
	public BattleModel battleModel;

	private int loadingStep;
	private float remainingLoadTime;

	public final PriorityQueueAssetManager assets;
	public final Map<String, Sprite> sprites;

	public DatabaseManager db;

	public Model() {
		controller = new ControllerHelper(this);

		scenes = new EnumMap<SceneType, Scene>(SceneType.class);
		sceneToShow = EmptyScene.instance;
		assets = new PriorityQueueAssetManager();
		sprites = new HashMap<String, Sprite>();

		assets.setLoader(TrainerProperties.class, new SynchronousAssetLoader<TrainerProperties, AssetLoaderParameters<TrainerProperties>>(new InternalFileHandleResolver()) {
			private final Json json = new Json();

			{
				/*json.setSerializer(CeldroidProperties.class, new Serializer<CeldroidProperties>() {
					@SuppressWarnings("rawtypes")
					@Override
					public void write(Json json, CeldroidProperties object, Class knownType) {
						throw new UnsupportedOperationException();
					}

					@SuppressWarnings("rawtypes")
					@Override
					public CeldroidProperties read(Json json, Object jsonData, Class type) {
						CeldroidProperties celdroid = assets.get(jsonData.toString(), CeldroidProperties.class);
						if (celdroid == null)
							throw new NullPointerException();
						return celdroid;
					}
				});*/
			}

			@SuppressWarnings("rawtypes")
			@Override
			public Array<AssetDescriptor> getDependencies(String fileName, FileHandle h, AssetLoaderParameters<TrainerProperties> parameter) {
				return null;
			}

			@Override
			public TrainerProperties load(AssetManager assetManager, String fileName, FileHandle h, AssetLoaderParameters<TrainerProperties> parameter) {
				return json.fromJson(TrainerProperties.class, resolve(fileName));
			}
		});
		Texture.setAssetManager(assets);
	}

	protected SceneFactory createSceneFactory() {
		return new SceneFactory();
	}

	public void onStart() {
		battleModel = new BattleModel(this);
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		// initialize scene instances
		SceneFactory sceneFactory = createSceneFactory();
		scenes.put(SceneType.LOAD_SCREEN, sceneFactory.makeLoadingScene(this));
		scenes.put(SceneType.MAIN_MENU, sceneFactory.makeMainMenuScene(this));
		scenes.put(SceneType.WORLD, sceneFactory.makeWorldScene(this));
		scenes.put(SceneType.BATTLE, sceneFactory.makeBattleScene(battleModel));
		scenes.putAll(sceneFactory.additionalScenes());
		sceneToShow = scenes.get(SceneType.LOAD_SCREEN);
		currentScene = scenes.get(SceneType.MAIN_MENU);
	}

	@SuppressWarnings("unchecked")
	public void startLoadingResources(boolean initialLoad) {
		Gdx.graphics.setContinuousRendering(true);
		remainingLoadTime = initialLoad ? Constants.SPLASH_SCREEN_MIN_TIME : 0;

		TextureParameter param = new TextureParameter();
		param.minFilter = param.magFilter = TextureFilter.Nearest;

		// synchronous load loading screen assets
		assets.queueOrMove("images/backgrounds/splash.png", Texture.class, param, 0);

		// asynchronous load all other assets
		assets.queueOrMove("fonts/buttons.fnt", BitmapFont.class, null, 1);
		assets.queueOrMove("images/sprites/common.pack.atlas", TextureAtlas.class, null, 1);
		for (Scene scene : scenes.values())
			for (PriorityQueueAssetManager.LoadEntry entry : scene.getAssetDependencies())
				assets.queueOrMove(entry.fileName, entry.type, entry.parameter, scene == currentScene ? 2 : 3);
		// TODO: load music using assets.load(..., Music.class),
		// and sound effects using assets.load(..., Sound.class)

		assets.startLoading();
		assets.finishLoading(0);
		loadingStep = 1;
	}

	public void continueLoadingResources(float tDelta) {
		//loading screen disappears as soon as all assets needed for the current
		//scene are loaded. loading of assets for other scenes continues in
		//background.
		if (loadingStep == 1) {
			//common assets
			remainingLoadTime -= tDelta;
			if (assets.update(loadingStep))
				loadingStep = 2;
		} else if (loadingStep == 2) {
			//current scene specific assets
			remainingLoadTime -= tDelta;
			if (assets.update(loadingStep)) {
				for (AtlasRegion reg : assets.get("images/sprites/common.pack.atlas", TextureAtlas.class).getRegions())
					sprites.put(reg.name, new Sprite(reg));
				for (String pack : currentScene.getSpriteSheetDependencies())
					for (AtlasRegion reg : assets.get(pack, TextureAtlas.class).getRegions())
						sprites.put(reg.name, new Sprite(reg));

				loadingStep = 3;
				if (remainingLoadTime <= 0)
					finishedLoadingResources();
			}
		} else {
			if (remainingLoadTime > 0) {
				//all assets loaded, but need to display load screen for minimum amount of time
				remainingLoadTime -= tDelta;
				if (remainingLoadTime <= 0 && loadingStep != 4) {
					finishedLoadingResources();
					if (loadingStep == -1)
						Gdx.graphics.setContinuousRendering(false);
				}
			}
			if (loadingStep == 3 || loadingStep == 4) {
				//remaining assets
				if (assets.update()) {
					for (Scene scene : scenes.values())
						for (String pack : scene.getSpriteSheetDependencies())
							for (AtlasRegion reg : assets.get(pack, TextureAtlas.class).getRegions())
								sprites.put(reg.name, new Sprite(reg));

					if (remainingLoadTime <= 0) {
						if (loadingStep == 4)
							finishedLoadingResources();
						Gdx.graphics.setContinuousRendering(false);
					}

					loadingStep = -1;
				}
			}
		}
	}

	private void finishedLoadingResources() {
		sceneToShow.swappedOut(true);
		sceneToShow = currentScene;
		sceneToShow.swappedIn(true);
		if (paused) {
			sceneToShow.resume();
			paused = false;
		}
	}

	//TODO: continuously render for a second or two. otherwise, Android's window
	//manager doesn't get a fresh screenshot of our app and will show an older
	//scene when animating to home or recents screen
	public void swapScene(Scene nextScene) {
		if (nextScene == null) {
			Gdx.app.exit();
			return;
		}

		sceneToShow.swappedOut(true);

		if (loadingStep != -1) {
			//swapping scene immediately after resume or create. assets for
			//other scenes still not fully loaded. put the loading screen back on.
			loadingStep = 4;
			currentScene = nextScene;
			sceneToShow = scenes.get(SceneType.LOAD_SCREEN);
		} else {
			sceneToShow = nextScene;
		}
		sceneToShow.swappedIn(true);
	}

	@Override
	public void resize(int screenWidth, int screenHeight) {
		super.resize(screenWidth, screenHeight);
		cam.position.set(Constants.WIDTH / 2, Constants.HEIGHT / 2, 0);
		cam.update();
		ScaleDisplay.Y_DOWN_FULL_SCREEN_DISPLAY.resize(screenWidth, screenHeight);
		for (Scene scene : scenes.values())
			scene.resize(screenWidth, screenHeight);
	}

	public void onPause() {
		sceneToShow.pause();
		if (sceneToShow != scenes.get(SceneType.LOAD_SCREEN)) {
			currentScene = sceneToShow;
			paused = true;
		}
	}

	public void onResume() {
		startLoadingResources(false);
		sceneToShow.swappedOut(false);
		sceneToShow = scenes.get(SceneType.LOAD_SCREEN);
		sceneToShow.swappedIn(false);
	}

	public void releaseAllResources() {
		assets.clear();
		sprites.clear();
	}

	public void onDispose() {
		assets.finishLoading();
		assets.dispose();
		if (db != null)
			db.closeAll();
	}
}
