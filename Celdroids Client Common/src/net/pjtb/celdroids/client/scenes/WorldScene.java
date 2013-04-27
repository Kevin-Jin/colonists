package net.pjtb.celdroids.client.scenes;

import java.util.EnumMap;
import java.util.Map;

public class WorldScene implements Scene {
	public enum WorldSubSceneType { IN_GAME_MENU }

	protected final Map<WorldSubSceneType, Scene> subScenes;
	private Scene subScene;

	public WorldScene() {
		subScenes = new EnumMap<WorldSubSceneType, Scene>(WorldSubSceneType.class);
		subScenes.put(WorldSubSceneType.IN_GAME_MENU, new InGameMenuScene());
	}

	@Override
	public void swappedIn() {
		
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
		//update our own scene stuff first, then subscene
		if (subScene != null)
			subScene.update(tDelta);
	}

	@Override
	public void draw() {
		//draw our own scene stuff first, then overlay with subscene
		if (subScene != null)
			subScene.draw();
	}

	@Override
	public void swappedOut() {
		
	}
}
