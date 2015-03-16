package in.kevinj.colonists.client;

import in.kevinj.colonists.client.loading.LoadingScene;
import in.kevinj.colonists.client.menu.MainMenuScene;
import in.kevinj.colonists.client.world.WorldModel;
import in.kevinj.colonists.client.world.WorldScene;

import java.util.Collections;
import java.util.Map;

public class SceneFactory {
	public LoadingScene makeLoadingScene(Model model) {
		return new LoadingScene(model);
	}

	public MainMenuScene makeMainMenuScene(Model model) {
		return new MainMenuScene(model);
	}

	public WorldScene makeWorldScene(WorldModel worldModel) {
		return new WorldScene(worldModel);
	}

	public Map<Model.SceneType, Scene> additionalScenes() {
		return Collections.emptyMap();
	}
}
