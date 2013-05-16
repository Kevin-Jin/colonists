package net.pjtb.celdroids.client;

import java.util.Collections;
import java.util.Map;

import net.pjtb.celdroids.client.battle.BattleScene;
import net.pjtb.celdroids.client.loading.LoadingScene;
import net.pjtb.celdroids.client.menu.MainMenuScene;
import net.pjtb.celdroids.client.world.WorldScene;

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

	public BattleScene makeBattleScene() {
		return new BattleScene();
	}

	public Map<Model.SceneType, Scene> additionalScenes() {
		return Collections.emptyMap();
	}
}
