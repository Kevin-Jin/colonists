package in.kevinj.colonists.client.world;

import in.kevinj.colonists.client.Button;
import in.kevinj.colonists.client.ConfirmPopupScene;
import in.kevinj.colonists.client.Model;
import in.kevinj.colonists.client.Scene;
import in.kevinj.colonists.client.world.menu.InGameMenuScene;

import java.util.EnumMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
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

	private Map<MapTile.ResourceType, Sprite> resourceTiles;
	private Map<MapTile.PortType, Sprite> portTiles;

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

		//flat topped axial coordinate grid implemented from http://www.redblobgames.com/grids/hexagons/
		//x increases diagonally down right, y increases straight up, z increases diagonally down left
		resourceTiles = new EnumMap<MapTile.ResourceType, Sprite>(MapTile.ResourceType.class);
		resourceTiles.put(MapTile.ResourceType.RICE, model.parent.sprites.get("environment/tileRice"));
		resourceTiles.put(MapTile.ResourceType.BAMBOO, model.parent.sprites.get("environment/tileBamboo"));
		resourceTiles.put(MapTile.ResourceType.HEMP, model.parent.sprites.get("environment/tileHemp"));
		resourceTiles.put(MapTile.ResourceType.IRON, model.parent.sprites.get("environment/tileIron"));
		resourceTiles.put(MapTile.ResourceType.STONE, model.parent.sprites.get("environment/tileStone"));
		resourceTiles.put(MapTile.ResourceType.WASTELAND, model.parent.sprites.get("environment/tileWasteland"));
		portTiles = new EnumMap<MapTile.PortType, Sprite>(MapTile.PortType.class);
		portTiles.put(MapTile.PortType.RICE, model.parent.sprites.get("environment/portRice"));
		portTiles.put(MapTile.PortType.BAMBOO, model.parent.sprites.get("environment/portBamboo"));
		portTiles.put(MapTile.PortType.HEMP, model.parent.sprites.get("environment/portHemp"));
		portTiles.put(MapTile.PortType.IRON, model.parent.sprites.get("environment/portIron"));
		portTiles.put(MapTile.PortType.STONE, model.parent.sprites.get("environment/portStone"));
		portTiles.put(MapTile.PortType.NONE, model.parent.sprites.get("environment/portNone"));
		Sprite textureToUse = resourceTiles.get(MapTile.ResourceType.WASTELAND);
		MapTile tile;
		int tileHeight = (int) textureToUse.getHeight();
		int tileWidth = (int) textureToUse.getWidth();

		staticTiles = new SpriteCache(model.mapBoundsColumns * model.mapBoundsRows, false);
		staticTiles.beginCache();
		for (int x = 0, offsetX = 0, offsetY = tileHeight; x < model.mapBoundsColumns; x++, offsetX += tileWidth / 4 * 3, offsetY -= (tileHeight + 1) / 2) {
			for (int y = 0; y < model.mapBoundsRows; y++) {
				tile = model.resources[y][x];
				if (tile == null) {
					continue;
				} else if (tile.isResource()) {
					textureToUse = resourceTiles.get(tile.getResourceType());
				} else {
					textureToUse = portTiles.get(tile.getPortType());
					textureToUse.setRotation(tile.getRotation());
				}

				textureToUse.setBounds(offsetX, offsetY + tileHeight * y, tileWidth, tileHeight);
				staticTiles.add(textureToUse);
			}
		}
		staticTilesCacheId = staticTiles.endCache();
	}

	private void drawChits(SpriteBatch batch) {
		Sprite textureToUse = resourceTiles.get(MapTile.ResourceType.WASTELAND);
		MapTile tile;
		int tileHeight = (int) textureToUse.getHeight();
		int tileWidth = (int) textureToUse.getWidth();
		for (int x = 0, offsetX = 0, offsetY = tileHeight; x < model.mapBoundsColumns; x++, offsetX += tileWidth / 4 * 3, offsetY -= (tileHeight + 1) / 2) {
			for (int y = 0; y < model.mapBoundsRows; y++) {
				tile = model.resources[y][x];
				if (tile == null || tile.getChit() == 0)
					continue;

				BitmapFont fnt = model.parent.assets.get("fonts/buttons.fnt", BitmapFont.class);
				fnt.setColor(1, 0, 0, 1);
				TextBounds bnds = fnt.getBounds(Integer.toString(tile.getChit()));
				//fnt.draw(batch, Integer.toString(tile.getChit()), offsetX + (tileWidth - bnds.width) / 2, offsetY + tileHeight * y - (tileHeight - bnds.height) / 2);
				fnt.draw(batch, Integer.toString(tile.getChit()), offsetX + (tileWidth - bnds.width) / 2, offsetY + tileHeight * y + bnds.height + 20);
			}
		}
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
		//for (Entity ent : model.animatedEntities)
			//ent.update(tDelta);
		model.updateActionButtonBehavior();
		if (model.actionButton.text != null)
			model.actionButton.update(tDelta);
		//model.cam.position.set(model.avatar.getScreenX(), model.avatar.getScreenY(), 1);
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
		Gdx.gl10.glEnable(GL10.GL_BLEND);
		Gdx.gl10.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		model.cam.apply(Gdx.gl10);
		staticTiles.draw(staticTilesCacheId);
		staticTiles.end();
		batch.begin();
		model.cam.apply(Gdx.gl10);
		drawChits(batch);
		//for (Entity ent : model.animatedEntities)
			//ent.draw(batch);
		batch.end();
		batch.begin();
		model.parent.cam.apply(Gdx.gl10);

		/*if (model.actionButton.text != null)
			model.actionButton.draw(batch);
		Sprite s = model.parent.sprites.get("ui/worldScene/controlBar");
		s.setBounds(960, 0, WorldModel.CONTROL_VIEW_WIDTH, Constants.HEIGHT);
		s.draw(batch);
		model.dpad.draw(batch);
		menuButton.draw(batch);
		backButton.draw(batch);*/

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
