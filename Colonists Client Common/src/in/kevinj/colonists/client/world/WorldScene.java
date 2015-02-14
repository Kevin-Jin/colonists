package in.kevinj.colonists.client.world;

import in.kevinj.colonists.Constants;
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
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class WorldScene implements Scene {
	public enum WorldSubSceneType {
		IN_GAME_MENU, CONFIRM_FLEE_POPUP
	}

	private static final int
		NO_DEBUG		= 0,
		DEBUG_TILES		= 1,
		DEBUG_VERTICES	= 2,
		DEBUG_EDGES		= 4
	;
	private static int DEBUG_MODE = NO_DEBUG;

	private final WorldModel model;

	protected final Map<WorldSubSceneType, Scene> subScenes;
	private Scene subScene;

	private Map<MapTile.ResourceType, Sprite> resourceTiles;
	private Map<MapTile.PortType, Sprite> portTiles;

	private final Button backButton, menuButton;

	private final ShapeRenderer shapeRenderer;
	private SpriteCache staticTiles;
	private int staticTilesCacheId;
	private int tileWidth, tileHeight;
	private BitmapFontCache staticChits;

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

		shapeRenderer = new ShapeRenderer();
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
		resourceTiles.put(MapTile.ResourceType.BROWNSTONE, model.parent.sprites.get("environment/tileBrownstone"));
		resourceTiles.put(MapTile.ResourceType.WASTELAND, model.parent.sprites.get("environment/tileWasteland"));
		portTiles = new EnumMap<MapTile.PortType, Sprite>(MapTile.PortType.class);
		portTiles.put(MapTile.PortType.RICE, model.parent.sprites.get("environment/portRice"));
		portTiles.put(MapTile.PortType.BAMBOO, model.parent.sprites.get("environment/portBamboo"));
		portTiles.put(MapTile.PortType.HEMP, model.parent.sprites.get("environment/portHemp"));
		portTiles.put(MapTile.PortType.IRON, model.parent.sprites.get("environment/portIron"));
		portTiles.put(MapTile.PortType.BROWNSTONE, model.parent.sprites.get("environment/portBrownstone"));
		portTiles.put(MapTile.PortType.PLAIN, model.parent.sprites.get("environment/portPlain"));
		portTiles.put(MapTile.PortType.NONE, model.parent.sprites.get("environment/portNone"));
		Sprite textureToUse = resourceTiles.get(MapTile.ResourceType.WASTELAND);
		MapTile tile;
		model.controller.tileHeight = tileHeight = (int) textureToUse.getHeight();
		model.controller.tileWidth = tileWidth = (int) textureToUse.getWidth();

		staticTiles = new SpriteCache(model.mapBoundsColumns * model.mapBoundsRows, false);
		staticTiles.beginCache();
		for (int x = 0, offsetX = 0, offsetY = tileHeight; x < model.mapBoundsColumns; x++, offsetX += tileWidth / 4 * 3, offsetY -= tileHeight / 2) {
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

		BitmapFont fnt = model.parent.assets.get("fonts/buttons.fnt", BitmapFont.class);
		staticChits = new BitmapFontCache(fnt);
		String message;
		TextBounds bnds;
		for (int x = 0, offsetX = 0, offsetY = tileHeight; x < model.mapBoundsColumns; x++, offsetX += tileWidth / 4 * 3, offsetY -= tileHeight / 2) {
			for (int y = 0; y < model.mapBoundsRows; y++) {
				tile = model.resources[y][x];
				if (tile == null || tile.getChit() == 0)
					continue;

				message = Integer.toString(tile.getChit());
				bnds = fnt.getBounds(message);
				staticChits.setColor(1, 0, 0, 1);
				staticChits.addText(message, offsetX + (tileWidth - bnds.width) / 2, offsetY + tileHeight * y + bnds.height + 20);
			}
		}
	}

	private void drawEntities(SpriteBatch batch) {
		Sprite road = model.parent.sprites.get("environment/road");
		Sprite village = model.parent.sprites.get("environment/village");
		Sprite metro = model.parent.sprites.get("environment/metro");
		Sprite highwayman = model.parent.sprites.get("environment/highwayman");
		for (Map.Entry<WorldModel.EntityCoordinate, Entity> entities : model.grid.entrySet()) {
			WorldModel.EntityCoordinate coord = entities.getKey();
			if (coord.isEdge()) {
				//draw road
				if (coord.xHundredths == 50) {
					//horizontal
					road.setPosition(
						tileWidth / 4 * 3 * coord.x + tileWidth / 4 * 3 - tileWidth / 2,
						tileHeight * (coord.y + 1) - tileHeight / 2 * coord.x - road.getHeight() / 2
					);
					road.setRotation(0);
				} else if (coord.yHundredths == 25) {
					//negative slope
					road.setPosition(
						tileWidth / 4 * 3 * coord.x + tileWidth / 4 * 3 - tileWidth / 2 + road.getHeight() / 2,
						tileHeight * (coord.y + 1) - tileHeight / 2 * coord.x
					);
					road.setRotation(120);
				} else if (coord.yHundredths == 75) {
					//positive slope
					road.setPosition(
						tileWidth / 4 * 3 * coord.x,
						tileHeight * (coord.y + 1) - tileHeight / 2 * (coord.x - 1) - road.getHeight()
					);
					road.setRotation(60);
				}
				road.setColor(1, 0.5f, 1, 1);
				road.setOrigin(0, 0);
				road.draw(batch);
			} else {
				//draw house
				Sprite sprite = Math.random() < 0.5 ? village : metro;
				sprite.setColor(1, 0.5f, 1, 1);
				if (coord.yHundredths == 50) {
					sprite.setPosition(
						tileWidth / 4 * 3 * coord.x - sprite.getWidth() / 2,
						tileHeight * (coord.y + 1) - tileHeight / 2 * (coord.x - 1) - sprite.getHeight() / 2
					);
				} else if (coord.yHundredths == 0) {
					sprite.setPosition(
						tileWidth / 4 * 3 * coord.x + tileWidth / 4 * 3 - tileWidth / 2 - sprite.getWidth() / 2,
						tileHeight * (coord.y + 1) - tileHeight / 2 * coord.x - sprite.getHeight() / 2
					);
				}
				sprite.draw(batch);
			}
			highwayman.setPosition(tileWidth / 4 * 3 * model.highwayman.x + highwayman.getWidth() / 2, tileHeight * model.highwayman.y - tileHeight / 2 * model.highwayman.x + tileHeight + (tileHeight - highwayman.getHeight()) / 2);
			highwayman.draw(batch);
		}
	}

	@Override
	public void resize(int width, int height) {
		model.resize(width, height);
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
		model.controller.hidden = (subScene != null);
		model.controller.update(tDelta);
		if (model.controller.getSelectedTile() != null)
			model.highwayman = model.controller.getSelectedTile();
		if (model.controller.getSelectedVertex() != null)
			if (model.grid.remove(model.controller.getSelectedVertex()) == null)
				model.grid.put(model.controller.getSelectedVertex(), model.avatar);
		if (model.controller.getSelectedEdge() != null)
			if (model.grid.remove(model.controller.getSelectedEdge()) == null)
				model.grid.put(model.controller.getSelectedEdge(), model.avatar);
		//for (Entity ent : model.animatedEntities)
			//ent.update(tDelta);
		model.updateActionButtonBehavior();
		if (model.actionButton.text != null)
			model.actionButton.update(tDelta);

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

		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		Gdx.gl.glViewport(0, 0, model.getScreenWidth(), model.getScreenHeight());
		shapeRenderer.setColor(0, 0, 0, 1);
		shapeRenderer.rect(0, Constants.HEIGHT, Constants.WIDTH, -Constants.HEIGHT);
		shapeRenderer.end();

		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		Gdx.gl.glViewport(model.getViewportX(), model.getViewportY(), model.getViewportWidth(), model.getViewportHeight());
		shapeRenderer.setColor(255, 255, 255, 1);
		//shapeRenderer.rect(0, model.screenHeight, model.screenWidth, -model.screenHeight);
		shapeRenderer.rect(0, 10000, 10000, -20000);
		shapeRenderer.end();

		staticTiles.begin();
		Gdx.gl10.glEnable(GL10.GL_BLEND);
		Gdx.gl10.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		model.getCamera().apply(Gdx.gl10);
		Gdx.gl.glViewport(model.getViewportX(), model.getViewportY(), model.getViewportWidth(), model.getViewportHeight());
		staticTiles.draw(staticTilesCacheId);
		staticTiles.end();

		if (DEBUG_MODE != NO_DEBUG) {
			shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
			model.getCamera().apply(Gdx.gl10);
			Gdx.gl.glViewport(model.getViewportX(), model.getViewportY(), model.getViewportWidth(), model.getViewportHeight());
			shapeRenderer.setColor(0, 0, 0, 1);
			//outline vertices
			if ((DEBUG_MODE & DEBUG_VERTICES) != 0)
				for (int x = 0, offsetX = tileWidth / 2, offsetY = tileHeight / 2; x <= model.mapBoundsColumns; x++, offsetX += tileWidth / 4 * 3, offsetY -= tileHeight / 2)
					for (int y = 0; y <= model.mapBoundsRows + 1; y++)
						shapeRenderer.triangle(
							offsetX,						offsetY + tileHeight * y,
							offsetX,						offsetY + tileHeight * y + tileHeight,
							offsetX - tileWidth / 4 * 3,	offsetY + tileHeight * y + tileHeight / 2
						);
						//shapeRenderer.triangle(offsetX, offsetY + tileHeight * y + tileHeight / 2, offsetX, offsetY + tileHeight * y - tileHeight / 2, offsetX + tileWidth / 4 * 3, offsetY + tileHeight * y);
			//outline edges
			if ((DEBUG_MODE & DEBUG_EDGES) != 0) {
				for (int x = 0, offsetX = -tileWidth / 4 * 3, offsetY = tileHeight + tileHeight / 2; x <= model.mapBoundsColumns; x++, offsetX += tileWidth / 4 * 3, offsetY -= tileHeight / 2) {
					for (int y = 0; y <= model.mapBoundsRows; y++) {
						shapeRenderer.polygon(new float[] {
							offsetX + tileWidth,							offsetY + tileHeight * y - (tileHeight + 1) / 2,
							offsetX + tileWidth / 2,						offsetY + tileHeight * y - (tileHeight + 1) / 2,
							offsetX + tileWidth / 4 * 3,					offsetY + tileHeight * y,
							offsetX + tileWidth / 4 * 3 + tileWidth / 2,	offsetY + tileHeight * y
						});
						shapeRenderer.polygon(new float[] {
							offsetX + tileWidth / 4 * 3,					offsetY + tileHeight * y,
							offsetX + tileWidth / 2,						offsetY + tileHeight * y + tileHeight / 2,
							offsetX + tileWidth,							offsetY + tileHeight * y + tileHeight / 2,
							offsetX + tileWidth + tileWidth / 4,			offsetY + tileHeight * y
						});
					}
				}
			}
			shapeRenderer.end();

			String message;
			BitmapFont fnt = model.parent.assets.get("fonts/buttons.fnt", BitmapFont.class);
			fnt.setColor(0, 0, 0, 1);
			TextBounds bnds;
			batch.begin();
			model.getCamera().apply(Gdx.gl10);
			Gdx.gl.glViewport(model.getViewportX(), model.getViewportY(), model.getViewportWidth(), model.getViewportHeight());
			//print hexagonal tile coordinates
			if ((DEBUG_MODE & DEBUG_TILES) != 0) {
				for (int x = 0, offsetX = 0, offsetY = tileHeight; x < model.mapBoundsColumns; x++, offsetX += tileWidth / 4 * 3, offsetY -= tileHeight / 2) {
					for (int y = 0; y < model.mapBoundsRows; y++) {
						message = "(" + x + ", " + y + ", " + (-x - y) + ")";
						bnds = fnt.getBounds(message);
						fnt.draw(batch, message, offsetX + (tileWidth - bnds.width) / 2, offsetY + tileHeight * (y + 1) - tileHeight / 2 + bnds.height / 2);
					}
				}
			}
			//print vertex coordinates
			if ((DEBUG_MODE & DEBUG_VERTICES) != 0) {
				for (int x = 0, offsetX = tileWidth / 2 - tileWidth / 4 * 3, offsetY = tileHeight / 2; x <= model.mapBoundsColumns; x++, offsetX += tileWidth / 4 * 3, offsetY -= tileHeight / 2) {
					for (int y = 0; y <= model.mapBoundsRows + 1; y++) {
						message = "(" + x + "," + y + ".5)";
						bnds = fnt.getBounds(message);
						fnt.draw(batch, message, offsetX, offsetY + tileHeight * (y + 1) + bnds.height / 2);
						message = "(" + (x - 1) + "," + y + ".0)";
						bnds = fnt.getBounds(message);
						fnt.draw(batch, message, offsetX - tileWidth / 4 * 3, offsetY + tileHeight * (y + 1) + bnds.height / 2);
					}
				}
			}
			batch.end();
		}

		batch.begin();
		model.getCamera().apply(Gdx.gl10);
		Gdx.gl.glViewport(model.getViewportX(), model.getViewportY(), model.getViewportWidth(), model.getViewportHeight());
		staticChits.draw(batch);
		drawEntities(batch);
		//for (Entity ent : model.animatedEntities)
			//ent.draw(batch);
		batch.end();
		batch.begin();
		model.parent.getCamera().apply(Gdx.gl10);

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
