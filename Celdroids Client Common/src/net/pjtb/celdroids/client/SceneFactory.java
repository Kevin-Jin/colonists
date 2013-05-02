package net.pjtb.celdroids.client;

import java.util.Collections;
import java.util.Map;

import net.pjtb.celdroids.client.scenes.BattleScene;
import net.pjtb.celdroids.client.scenes.LoadingScene;
import net.pjtb.celdroids.client.scenes.MainMenuScene;
import net.pjtb.celdroids.client.scenes.Scene;
import net.pjtb.celdroids.client.scenes.WorldScene;

public class SceneFactory {
	public LoadingScene makeLoadingScene(Model model) {
		return new LoadingScene(model);
	}

	public MainMenuScene makeMainMenuScene() {
		return new MainMenuScene();
	}

	public WorldScene makeWorldScene() {
		return new WorldScene();
	}

	public BattleScene makeBattleScene() {
		return new BattleScene();
	}

	public Map<Model.SceneType, Scene> additionalScenes() {
		return Collections.emptyMap();
	}
}
