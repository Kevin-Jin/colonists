package in.kevinj.colonists.client.world;

import java.util.EnumMap;
import java.util.Map;

import in.kevinj.colonists.Constants;
import in.kevinj.colonists.client.Button;
import in.kevinj.colonists.client.ConfirmPopupScene;
import in.kevinj.colonists.client.Model;
import in.kevinj.colonists.client.Scene;
import in.kevinj.colonists.client.world.menu.InGameMenuScene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteCache;

public class WorldScene implements Scene {
	public enum WorldSubSceneType {
		IN_GAME_MENU, CONFIRM_FLEE_POPUP
	}

	private final WorldModel model;

	protected final Map<WorldSubSceneType, Scene> subScenes;
	private Scene subScene;

	private final Button backButton, menuButton;

	private SpriteCache staticTiles;
	private int staticTilesCacheId;

	public WorldScene(Model m) {
		this.model = new WorldModel(m);

		subScenes = new EnumMap<WorldSubSceneType, Scene>(WorldSubSceneType.class);
		subScenes.put(WorldSubSceneType.IN_GAME_MENU, new InGameMenuScene(m, this));
		subScenes.put(WorldSubSceneType.CONFIRM_FLEE_POPUP, new ConfirmPopupScene(m, "Are you sure you want to return to the menu?", Model.SceneType.MAIN_MENU));

		backButton = new Button(m, null, new Runnable() {
			@Override
			public void run() {
				confirmBack();
			}
		}, 1172, 576, 108, 144, "ui/worldScene/back", "ui/worldScene/selectedBack", 255, 255, 255, 255, -1, -1, -1, -1);
		menuButton = new Button(m, null, new Runnable() {
			@Override
			public void run() {
				openPopupMenu();
			}
		}, 1172, 0, 108, 144, "ui/worldScene/more", "ui/worldScene/selectedMore", 255, 255, 255, 255, -1, -1, -1, -1);
	}

	@Override
	public void swappedIn(boolean transition) {
		Gdx.gl10.glClearColor(1, 1, 1, 1);

		staticTiles = new SpriteCache(model.mapBoundsColumns * model.mapBoundsRows, false);
		staticTiles.beginCache();
		Sprite s = model.parent.sprites.get("environment/grass1");
		for (int i = 0; i < model.mapBoundsColumns; i++) {
			for (int j = 0; j < model.mapBoundsRows; j++) {
				s.setBounds(i * WorldModel.TILE_SIZE, j * WorldModel.TILE_SIZE, WorldModel.TILE_SIZE, WorldModel.TILE_SIZE);
				staticTiles.add(s);
			}
		}
		staticTilesCacheId = staticTiles.endCache();
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

	private void confirmBack() {
		subScene = subScenes.get(WorldSubSceneType.CONFIRM_FLEE_POPUP);
		subScene.swappedIn(true);
	}

	private void openPopupMenu() {
		subScene = subScenes.get(WorldSubSceneType.IN_GAME_MENU);
		subScene.swappedIn(true);
	}

	@Override
	public void update(float tDelta) {
		backButton.hidden = (subScene != null);
		backButton.update(tDelta);
		menuButton.hidden = (subScene != null);
		menuButton.update(tDelta);
		model.dpad.hidden = (subScene != null);
		model.dpad.update(tDelta);
		for (Entity ent : model.animatedEntities)
			ent.update(tDelta);
		model.updateActionButtonBehavior();
		if (model.actionButton.text != null)
			model.actionButton.update(tDelta);
		model.cam.position.set(model.avatar.getScreenX(), model.avatar.getScreenY(), 1);
		model.cam.update();

		if (subScene == null) {
			if (model.parent.controller.wasBackPressed && !Gdx.input.isKeyPressed(Keys.ESCAPE) && !Gdx.input.isKeyPressed(Keys.BACK)) {
				confirmBack();
			} else if (model.parent.controller.wasMenuPressed && !Gdx.input.isKeyPressed(Keys.ENTER) && !Gdx.input.isKeyPressed(Keys.MENU)) {
				openPopupMenu();
			}
		} else {
			subScene.update(tDelta);
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		batch.end();
		staticTiles.begin();
		model.cam.apply(Gdx.gl10);
		staticTiles.draw(staticTilesCacheId);
		staticTiles.end();
		batch.begin();
		model.cam.apply(Gdx.gl10);
		for (Entity ent : model.animatedEntities)
			ent.draw(batch);
		batch.end();
		batch.begin();
		model.parent.cam.apply(Gdx.gl10);

		if (model.actionButton.text != null)
			model.actionButton.draw(batch);
		Sprite s = model.parent.sprites.get("ui/worldScene/controlBar");
		s.setBounds(960, 0, WorldModel.CONTROL_VIEW_WIDTH, Constants.HEIGHT);
		s.draw(batch);
		model.dpad.draw(batch);
		menuButton.draw(batch);
		backButton.draw(batch);

		if (subScene != null)
			subScene.draw(batch);
	}

	@Override
	public void swappedOut(boolean transition) {
		
	}

	@Override
	public Scene getSubscene() {
		return subScene;
	}

	@Override
	public void setSubscene(Scene scene) {
		this.subScene = scene;
	}
}
