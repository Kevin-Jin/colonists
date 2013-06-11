package net.pjtb.celdroids.client;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import net.pjtb.celdroids.client.battle.BattleModel;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializer;

public class Model {
	public enum SceneType { LOAD_SCREEN, MAIN_MENU, WORLD, BATTLE }

	public final ControllerHelper controller;

	public final Map<SceneType, Scene> scenes;
	public Scene scene;
	private Scene pausedScene;
	public BattleModel battleModel;

	private boolean loading;
	private float remainingLoadTime;

	public final AssetManager assets;
	public final Map<String, Sprite> sprites;

	public Model() {
		controller = new ControllerHelper();

		scenes = new EnumMap<SceneType, Scene>(SceneType.class);
		scene = EmptyScene.instance;
		assets = new AssetManager();
		sprites = new HashMap<String, Sprite>();

		assets.setLoader(CeldroidBattleMove.class, new SynchronousAssetLoader<CeldroidBattleMove, AssetLoaderParameters<CeldroidBattleMove>>(new InternalFileHandleResolver()) {
			private final Json json = new Json();

			@SuppressWarnings("rawtypes")
			@Override
			public Array<AssetDescriptor> getDependencies(String fileName, AssetLoaderParameters<CeldroidBattleMove> parameter) {
				return null;
			}

			@Override
			public CeldroidBattleMove load(AssetManager assetManager, String fileName, AssetLoaderParameters<CeldroidBattleMove> parameter) {
				CeldroidBattleMove move = json.fromJson(CeldroidBattleMove.class, resolve(fileName));
				move.file = fileName;
				return move;
			}
		});
		assets.setLoader(CeldroidProperties.class, new SynchronousAssetLoader<CeldroidProperties, AssetLoaderParameters<CeldroidProperties>>(new InternalFileHandleResolver()) {
			private final Json json = new Json();

			{
				json.setSerializer(CeldroidBattleMove.class, new Serializer<CeldroidBattleMove>() {
					@SuppressWarnings("rawtypes")
					@Override
					public void write(Json json, CeldroidBattleMove object, Class knownType) {
						throw new UnsupportedOperationException();
					}

					@SuppressWarnings("rawtypes")
					@Override
					public CeldroidBattleMove read(Json json, Object jsonData, Class type) {
						CeldroidBattleMove move = assets.get(jsonData.toString());
						if (move == null)
							throw new NullPointerException();
						return move;
					}
				});
			}

			@SuppressWarnings("rawtypes")
			@Override
			public Array<AssetDescriptor> getDependencies(String fileName, AssetLoaderParameters<CeldroidProperties> parameter) {
				return null;
			}

			@Override
			public CeldroidProperties load(AssetManager assetManager, String fileName, AssetLoaderParameters<CeldroidProperties> parameter) {
				return json.fromJson(CeldroidProperties.class, resolve(fileName));
			}
		});
		assets.setLoader(TrainerProperties.class, new SynchronousAssetLoader<TrainerProperties, AssetLoaderParameters<TrainerProperties>>(new InternalFileHandleResolver()) {
			private final Json json = new Json();

			{
				json.setSerializer(CeldroidProperties.class, new Serializer<CeldroidProperties>() {
					@SuppressWarnings("rawtypes")
					@Override
					public void write(Json json, CeldroidProperties object, Class knownType) {
						throw new UnsupportedOperationException();
					}

					@SuppressWarnings("rawtypes")
					@Override
					public CeldroidProperties read(Json json, Object jsonData, Class type) {
						CeldroidProperties celdroid = assets.get(jsonData.toString());
						if (celdroid == null)
							throw new NullPointerException();
						return celdroid;
					}
				});
			}

			@SuppressWarnings("rawtypes")
			@Override
			public Array<AssetDescriptor> getDependencies(String fileName, AssetLoaderParameters<TrainerProperties> parameter) {
				return null;
			}

			@Override
			public TrainerProperties load(AssetManager assetManager, String fileName, AssetLoaderParameters<TrainerProperties> parameter) {
				return json.fromJson(TrainerProperties.class, resolve(fileName));
			}
		});
	}

	protected SceneFactory createSceneFactory() {
		return new SceneFactory();
	}

	public void onStart() {
		battleModel = new BattleModel(this);

		//initialize scene instances
		SceneFactory sceneFactory = createSceneFactory();
		scenes.put(SceneType.LOAD_SCREEN, sceneFactory.makeLoadingScene(this));
		scenes.put(SceneType.MAIN_MENU, sceneFactory.makeMainMenuScene(this));
		scenes.put(SceneType.WORLD, sceneFactory.makeWorldScene(this));
		scenes.put(SceneType.BATTLE, sceneFactory.makeBattleScene(battleModel));
		scenes.putAll(sceneFactory.additionalScenes());
		scene = scenes.get(SceneType.LOAD_SCREEN);
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
		assets.load("images/backgrounds/titleScreen.png", Texture.class, param);
		assets.load("moves/bubble.json", CeldroidBattleMove.class);
		assets.load("moves/fire.json", CeldroidBattleMove.class);
		assets.load("moves/rock.json", CeldroidBattleMove.class);
		assets.load("monsters/fire1.json", CeldroidProperties.class);
		assets.load("monsters/rock1.json", CeldroidProperties.class);
		assets.load("monsters/rock2.json", CeldroidProperties.class);
		assets.load("monsters/water1.json", CeldroidProperties.class);
		assets.load("trainers/red.json", TrainerProperties.class);
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
			scene.swappedIn(true);
			scene.resume();
			pausedScene = null;
		} else {
			scene = scenes.get(SceneType.MAIN_MENU);
			scene.swappedIn(true);
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
