package in.kevinj.colonists.client.world;

import in.kevinj.colonists.Constants;
import in.kevinj.colonists.client.Button;
import in.kevinj.colonists.client.ConfirmPopupScene;
import in.kevinj.colonists.client.ContinuousRendererUtil;
import in.kevinj.colonists.client.Model;
import in.kevinj.colonists.client.PriorityQueueAssetManager;
import in.kevinj.colonists.client.PriorityQueueAssetManager.LoadEntry;
import in.kevinj.colonists.client.ScaleDisplay;
import in.kevinj.colonists.client.Scene;
import in.kevinj.colonists.client.world.WorldModel.Loupe;
import in.kevinj.colonists.client.world.menu.InGameMenuScene;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

//TODO: use LibGDX Stage/Actor + GestureDetector in place of our ScaleDisplay + MapInteraction
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
	public Collection<LoadEntry> getAssetDependencies() {
		return Arrays.asList(
			new PriorityQueueAssetManager.LoadEntry("images/sprites/worldScene.pack.atlas", TextureAtlas.class, null)/*,
			new PriorityQueueAssetManager.LoadEntry("trainers/red.json", TrainerProperties.class, null)*/
		);
	}

	@Override
	public Collection<String> getSpriteSheetDependencies() {
		return Collections.singleton("images/sprites/worldScene.pack.atlas");
	}

	@Override
	public void swappedIn(boolean transition) {
		Gdx.gl20.glClearColor(1, 1, 1, 1);

		//flat topped axial coordinate grid implemented from http://www.redblobgames.com/grids/hexagons/
		//x increases diagonally down right, y increases straight up, z increases diagonally down left
		resourceTiles = new EnumMap<MapTile.ResourceType, Sprite>(MapTile.ResourceType.class);
		resourceTiles.put(MapTile.ResourceType.RICE, model.parent.sprites.get("map/tileRice"));
		resourceTiles.put(MapTile.ResourceType.BAMBOO, model.parent.sprites.get("map/tileBamboo"));
		resourceTiles.put(MapTile.ResourceType.HEMP, model.parent.sprites.get("map/tileHemp"));
		resourceTiles.put(MapTile.ResourceType.IRON, model.parent.sprites.get("map/tileIron"));
		resourceTiles.put(MapTile.ResourceType.BROWNSTONE, model.parent.sprites.get("map/tileBrownstone"));
		resourceTiles.put(MapTile.ResourceType.WASTELAND, model.parent.sprites.get("map/tileWasteland"));
		portTiles = new EnumMap<MapTile.PortType, Sprite>(MapTile.PortType.class);
		portTiles.put(MapTile.PortType.RICE, model.parent.sprites.get("map/portRice"));
		portTiles.put(MapTile.PortType.BAMBOO, model.parent.sprites.get("map/portBamboo"));
		portTiles.put(MapTile.PortType.HEMP, model.parent.sprites.get("map/portHemp"));
		portTiles.put(MapTile.PortType.IRON, model.parent.sprites.get("map/portIron"));
		portTiles.put(MapTile.PortType.BROWNSTONE, model.parent.sprites.get("map/portBrownstone"));
		portTiles.put(MapTile.PortType.PLAIN, model.parent.sprites.get("map/portPlain"));
		portTiles.put(MapTile.PortType.NONE, model.parent.sprites.get("map/portNone"));
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

	private void setEdgeSpritePosition(Sprite road, WorldModel.EntityCoordinate coord) {
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
		road.setOrigin(0, 0);
	}

	private void setVertexSpritePosition(Sprite sprite, WorldModel.EntityCoordinate coord) {
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
	}

	private void drawEntities(SpriteBatch batch) {
		Sprite road = model.parent.sprites.get("map/road");
		Sprite village = model.parent.sprites.get("map/village");
		Sprite metro = model.parent.sprites.get("map/metro");
		Sprite highwayman = model.parent.sprites.get("map/highwayman");
		for (Map.Entry<WorldModel.EntityCoordinate, Entity> entities : model.grid.entrySet()) {
			WorldModel.EntityCoordinate coord = entities.getKey();
			if (coord.isEdge()) {
				//draw road
				setEdgeSpritePosition(road, coord);
				road.setColor(1, 0.5f, 1, 1);
				road.draw(batch);
			} else {
				//draw house
				Sprite sprite = Math.random() < 0.5 ? village : metro;
				setVertexSpritePosition(sprite, coord);
				sprite.setColor(1, 0.5f, 1, 1);
				sprite.draw(batch);
			}
			highwayman.setPosition(tileWidth / 4 * 3 * model.highwayman.x + highwayman.getWidth() / 2, tileHeight * model.highwayman.y - tileHeight / 2 * model.highwayman.x + tileHeight + (tileHeight - highwayman.getHeight()) / 2);
			highwayman.setColor(1, 1, 1, 1);
			highwayman.draw(batch);
		}

		if (model.roadCandidate != null) {
			setEdgeSpritePosition(road, model.roadCandidate);
			if (model.grid.containsKey(model.roadCandidate))
				road.setColor(1, 0, 0, 0.8f);
			else
				road.setColor(0, 1, 0, 0.8f);
			road.draw(batch);
		}
		if (model.metroCandidate != null) {
			setVertexSpritePosition(metro, model.metroCandidate);
			if (model.grid.containsKey(model.metroCandidate))
				metro.setColor(1, 0, 0, 0.6f);
			else
				metro.setColor(0, 1, 0, 0.6f);
			metro.draw(batch);
		}
		if (model.villageCandidate != null) {
			setVertexSpritePosition(village, model.villageCandidate);
			if (model.grid.containsKey(model.villageCandidate))
				village.setColor(1, 0, 0, 0.6f);
			else
				village.setColor(0, 1, 0, 0.6f);
			village.draw(batch);
		}
		if (model.highwaymanCandidate != null) {
			highwayman.setPosition(tileWidth / 4 * 3 * model.highwaymanCandidate.x + highwayman.getWidth() / 2, tileHeight * model.highwaymanCandidate.y - tileHeight / 2 * model.highwaymanCandidate.x + tileHeight + (tileHeight - highwayman.getHeight()) / 2);
			if (model.highwayman.equals(model.highwaymanCandidate))
				highwayman.setColor(1, 0, 0, 0.6f);
			else
				highwayman.setColor(0, 1, 0, 0.6f);
			highwayman.draw(batch);
		}

		//TODO: black out map where we're not allowed to place a road, house, highwayman.
		//use a stencil for this. draw valid roads, houses, and highwayman that can be
		//placed in the stencil buffer and draw rectangle with color (0, 0, 0, 0.5f) on
		//area not drawn in the stencil
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
		setSubscene(subScenes.get(WorldSubSceneType.CONFIRM_FLEE_POPUP));
		subScene.swappedIn(true);
	}

	private void openPopupMenu() {
		setSubscene(subScenes.get(WorldSubSceneType.IN_GAME_MENU));
		subScene.swappedIn(true);
	}

	@Override
	public void update(float tDelta) {
		backButton.hidden = (subScene != null);
		backButton.update(tDelta);
		menuButton.hidden = (subScene != null);
		menuButton.update(tDelta);
		model.loupe.update(tDelta);
		model.controller.hidden = (subScene != null);
		model.controller.update(tDelta);
		if (model.controller.getSelectedTile(false) != null) {
			model.highwaymanCandidate = model.controller.getSelectedTile(false);
		} else {
			if (model.controller.getSelectedTile(true) != null)
				model.highwayman = model.controller.getSelectedTile(true);
			model.highwaymanCandidate = null;
		}
		if (model.controller.getSelectedVertex(false) != null) {
			model.metroCandidate = model.controller.getSelectedVertex(false);
		} else {
			if (model.controller.getSelectedVertex(true) != null)
				if (model.grid.remove(model.controller.getSelectedVertex(true)) == null)
					model.grid.put(model.controller.getSelectedVertex(true), model.avatar);
			model.metroCandidate = null;
		}
		if (model.controller.getSelectedEdge(false) != null) {
			model.roadCandidate = model.controller.getSelectedEdge(false);
		} else {
			if (model.controller.getSelectedEdge(true) != null)
				if (model.grid.remove(model.controller.getSelectedEdge(true)) == null)
					model.grid.put(model.controller.getSelectedEdge(true), model.avatar);
			model.roadCandidate = null;
		}
		//for (Entity ent : model.animatedEntities)
			//ent.update(tDelta);
		model.updateActionButtonBehavior();
		if (model.actionButton.text != null)
			model.actionButton.update(tDelta);

		if (subScene == null) {
			if (model.parent.controller.wasBackPressed && !Gdx.input.isKeyPressed(Keys.ESCAPE) && !Gdx.input.isKeyPressed(Keys.BACK)) {
				confirmBack();
			} else if (model.parent.controller.wasMenuPressed && !Gdx.input.isKeyPressed(Keys.ENTER) && !Gdx.input.isKeyPressed(Keys.MENU)) {
				//openPopupMenu();
			}
		} else {
			subScene.update(tDelta);
		}
	}

	private void draw(SpriteBatch batch, Camera cam, int viewportX, int viewportY, int viewportWidth, int viewportHeight) {
		//clear the canvas
		shapeRenderer.setProjectionMatrix(cam.combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		Gdx.gl20.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);
		shapeRenderer.setColor(1, 1, 1, 1);
		shapeRenderer.rect(
			-model.getCamera().viewportWidth * (model.getCamera().zoom - 2) / 2,
			//(-model.getCamera().viewportHeight * (model.getCamera().zoom - 2) / 2) + (model.getCamera().viewportHeight * model.getCamera().zoom) - (model.getCamera().position.y + Constants.HEIGHT - model.getCamera().viewportHeight),
			model.getCamera().viewportHeight * model.getCamera().zoom / 2 - model.getCamera().position.y + Constants.HEIGHT,
			model.getCamera().viewportWidth * model.getCamera().zoom,
			-model.getCamera().viewportHeight * model.getCamera().zoom
		);
		shapeRenderer.end();

		staticTiles.setProjectionMatrix(cam.combined);
		staticTiles.begin();
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl20.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);
		staticTiles.draw(staticTilesCacheId);
		staticTiles.end();

		if (DEBUG_MODE != NO_DEBUG) {
			shapeRenderer.setProjectionMatrix(cam.combined);
			shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
			Gdx.gl20.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);
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
			batch.setProjectionMatrix(cam.combined);
			batch.begin();
			Gdx.gl20.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);
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

		batch.setProjectionMatrix(cam.combined);
		batch.begin();
		Gdx.gl20.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);
		staticChits.draw(batch);
		drawEntities(batch);
		//for (Entity ent : model.animatedEntities)
			//ent.draw(batch);
		batch.end();
	}

	@Override
	public void draw(SpriteBatch batch) {
		batch.end();

		//clear the pillar/letter boxing
		shapeRenderer.setProjectionMatrix(ScaleDisplay.Y_DOWN_FULL_SCREEN_DISPLAY.getCamera().combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		Gdx.gl20.glViewport(0, 0, model.getScreenWidth(), model.getScreenHeight());
		shapeRenderer.setColor(0, 0, 0, 1);
		shapeRenderer.rect(0, 0, ScaleDisplay.Y_DOWN_FULL_SCREEN_DISPLAY.getCamera().viewportWidth, ScaleDisplay.Y_DOWN_FULL_SCREEN_DISPLAY.getCamera().viewportHeight);
		shapeRenderer.end();

		//draw on the canvas
		draw(batch, model.getCamera(), model.getViewportX(), model.getViewportY(), model.getViewportWidth(), model.getViewportHeight());

		if (!model.loupe.hidden) {
			Vector3 yDownCursor = model.parent.controller.getCursor(ScaleDisplay.Y_DOWN_FULL_SCREEN_DISPLAY);
			//begin drawing an outline around the loupe
			shapeRenderer.setProjectionMatrix(ScaleDisplay.Y_DOWN_FULL_SCREEN_DISPLAY.getCamera().combined);
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			Gdx.gl20.glViewport(ScaleDisplay.Y_DOWN_FULL_SCREEN_DISPLAY.getViewportX(), ScaleDisplay.Y_DOWN_FULL_SCREEN_DISPLAY.getViewportY(), ScaleDisplay.Y_DOWN_FULL_SCREEN_DISPLAY.getViewportWidth(), ScaleDisplay.Y_DOWN_FULL_SCREEN_DISPLAY.getViewportHeight());
			shapeRenderer.setColor(0, 0, 0, 1);
			shapeRenderer.circle(yDownCursor.x, yDownCursor.y - Loupe.RADIUS - Loupe.STEM_HEIGHT, Loupe.RADIUS + 8);
			shapeRenderer.triangle(yDownCursor.x - Loupe.STEM_WIDTH / 2, yDownCursor.y - Loupe.RADIUS - Loupe.STEM_HEIGHT - 8, yDownCursor.x + Loupe.STEM_WIDTH / 2, yDownCursor.y - Loupe.RADIUS - Loupe.STEM_HEIGHT - 8, yDownCursor.x, yDownCursor.y);
			shapeRenderer.end();
			//end drawing an outline
	
			//begin positioning and shaping the loupe
			Gdx.gl20.glEnable(GL20.GL_STENCIL_TEST);
			Gdx.gl20.glColorMask(false, false, false, false);
			Gdx.gl20.glDepthMask(false);
			Gdx.gl20.glStencilFunc(GL20.GL_NEVER, 1, 0xFF);
			Gdx.gl20.glStencilOp(GL20.GL_REPLACE, GL20.GL_KEEP, GL20.GL_KEEP);
			Gdx.gl20.glStencilMask(0xFF);
			Gdx.gl20.glClear(GL20.GL_STENCIL_BUFFER_BIT);
			//ShapeRenderer has problems with Fill when using the default y-up coordinate system
			shapeRenderer.setProjectionMatrix(ScaleDisplay.Y_DOWN_FULL_SCREEN_DISPLAY.getCamera().combined);
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			Gdx.gl20.glViewport(ScaleDisplay.Y_DOWN_FULL_SCREEN_DISPLAY.getViewportX(), ScaleDisplay.Y_DOWN_FULL_SCREEN_DISPLAY.getViewportY(), ScaleDisplay.Y_DOWN_FULL_SCREEN_DISPLAY.getViewportWidth(), ScaleDisplay.Y_DOWN_FULL_SCREEN_DISPLAY.getViewportHeight());
			shapeRenderer.setColor(0, 0, 0, 1);
			shapeRenderer.circle(yDownCursor.x, yDownCursor.y - Loupe.RADIUS - Loupe.STEM_HEIGHT, Loupe.RADIUS);
			shapeRenderer.end();
			Gdx.gl20.glColorMask(true, true, true, true);
			Gdx.gl20.glDepthMask(true);
			Gdx.gl20.glStencilMask(0x00);
			//end positioning and shaping loupe position
	
			//begin drawing inside the loupe
			Gdx.gl20.glStencilFunc(GL20.GL_EQUAL, 0, 0xFF);
			Gdx.gl20.glStencilFunc(GL20.GL_EQUAL, 1, 0xFF);
			draw(batch, model.loupe.getCamera(), model.loupe.getViewportX(), model.loupe.getViewportY(), model.loupe.getViewportWidth(), model.loupe.getViewportHeight());
			Vector3 cursor = model.parent.controller.getCursor(model);
			shapeRenderer.setProjectionMatrix(model.loupe.getCamera().combined);
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			Gdx.gl20.glViewport(model.loupe.getViewportX(), model.loupe.getViewportY(), model.loupe.getViewportWidth(), model.loupe.getViewportHeight());
			shapeRenderer.setColor(0, 0, 0, 1);
			shapeRenderer.rect(cursor.x - 25, cursor.y + 25, 50, -50);
			shapeRenderer.end();
			Gdx.gl20.glDisable(GL20.GL_STENCIL_TEST);
			//end drawing inside the loupe
		}

		batch.setProjectionMatrix(model.getCamera().combined);
		batch.begin();
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
		//continuously render for a bit. otherwise, Android's window manager
		//doesn't get a fresh screenshot of our app and will show an older scene
		//when animating to home or recents screen
		ContinuousRendererUtil.instance.doShortContinuousRender();

		this.subScene = scene;
	}
}
