package in.kevinj.colonists.client;

import java.util.Collections;
import java.util.Map;

import in.kevinj.colonists.client.loading.LoadingScene;
import in.kevinj.colonists.client.menu.MainMenuScene;
import in.kevinj.colonists.client.world.BattleModel;
import in.kevinj.colonists.client.world.BattleScene;
import in.kevinj.colonists.client.world.WorldScene;

public class SceneFactory {
	public LoadingScene makeLoadingScene(Model model) {
		return new LoadingScene(model);
	}

	public MainMenuScene makeMainMenuScene(Model model) {
		return new MainMenuScene(model);
	}

	public WorldScene makeWorldScene(Model model) {
		return new WorldScene(model);
	}

	public BattleScene makeBattleScene(BattleModel battleModel) {
		return new BattleScene(battleModel);
	}

	public Map<Model.SceneType, Scene> additionalScenes() {
		return Collections.emptyMap();
	}
}
