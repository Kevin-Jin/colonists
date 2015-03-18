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
import in.kevinj.colonists.world.Coordinate;
import in.kevinj.colonists.world.MapTile;
import in.kevinj.colonists.world.PlayerAction;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
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
	private final GraphicalEntity.Village greenHighlightVillage, redHighlightVillage;
	private final GraphicalEntity.Metro greenHighlightMetro, redHighlightMetro;
	private final GraphicalEntity.Road greenHighlightRoad, redHighlightRoad;
	private final GraphicalEntity.Highwayman greenHighlightHighwayman, redHighlightHighwayman;

	private final Button backButton, menuButton;

	private final ShapeRenderer shapeRenderer;
	private SpriteCache staticTiles;
	private int staticTilesCacheId;
	private BitmapFontCache staticChits;

	private float fogTransparency;
	private boolean isUpdatingFog;

	public WorldScene(WorldModel m) {
		this.model = m;

		subScenes = new EnumMap<WorldSubSceneType, Scene>(WorldSubSceneType.class);
		subScenes.put(WorldSubSceneType.IN_GAME_MENU, new InGameMenuScene(m.parent, this));
		subScenes.put(WorldSubSceneType.CONFIRM_FLEE_POPUP, new ConfirmPopupScene(m.parent, "Are you sure you want to return to the menu?", Model.SceneType.MAIN_MENU));

		backButton = new Button(m.parent, null, new Runnable() {
			@Override
			public void run() {
				confirmBack();
			}
		}, 1172, 576, 108, 144, "ui/worldScene/back", "ui/worldScene/selectedBack", 255, 255, 255, 255, -1, -1, -1, -1);
		menuButton = new Button(m.parent, null, new Runnable() {
			@Override
			public void run() {
				openPopupMenu();
			}
		}, 1172, 0, 108, 144, "ui/worldScene/more", "ui/worldScene/selectedMore", 255, 255, 255, 255, -1, -1, -1, -1);

		shapeRenderer = new ShapeRenderer();

		greenHighlightVillage = new GraphicalEntity.Village(model, true);
		redHighlightVillage = new GraphicalEntity.Village(model, false);
		greenHighlightMetro = new GraphicalEntity.Metro(model, true);
		redHighlightMetro = new GraphicalEntity.Metro(model, false);
		greenHighlightRoad = new GraphicalEntity.Road(model, true);
		redHighlightRoad = new GraphicalEntity.Road(model, false);
		greenHighlightHighwayman = new GraphicalEntity.Highwayman(model, true);
		redHighlightHighwayman = new GraphicalEntity.Highwayman(model, false);
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
		model.tileHeight = (int) textureToUse.getHeight();
		model.tileWidth = (int) textureToUse.getWidth();
		model.settlementRadius = (int) (model.parent.sprites.get("map/metro").getHeight() / 2);

		staticTiles = new SpriteCache(model.mapBoundsColumns * model.mapBoundsRows, false);
		staticTiles.beginCache();
		for (int x = 0, offsetX = 0, offsetY = model.tileHeight; x < model.mapBoundsColumns; x++, offsetX += model.tileWidth / 4 * 3, offsetY -= model.tileHeight / 2) {
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

				textureToUse.setBounds(offsetX, offsetY + model.tileHeight * y, model.tileWidth, model.tileHeight);
				staticTiles.add(textureToUse);
			}
		}
		staticTilesCacheId = staticTiles.endCache();

		BitmapFont fnt = model.parent.assets.get("fonts/buttons.fnt", BitmapFont.class);
		staticChits = new BitmapFontCache(fnt);
		String message;
		TextBounds bnds;
		for (int x = 0, offsetX = 0, offsetY = model.tileHeight; x < model.mapBoundsColumns; x++, offsetX += model.tileWidth / 4 * 3, offsetY -= model.tileHeight / 2) {
			for (int y = 0; y < model.mapBoundsRows; y++) {
				tile = model.resources[y][x];
				if (tile == null || tile.getChit() == 0)
					continue;

				message = Integer.toString(tile.getChit());
				bnds = fnt.getBounds(message);
				staticChits.setColor(1, 0, 0, 1);
				staticChits.addText(message, offsetX + (model.tileWidth - bnds.width) / 2, offsetY + model.tileHeight * y + bnds.height + 20);
			}
		}

		Gdx.input.setCursorCatched(subScene == null);
	}

	private void drawEntities(SpriteBatch batch, Set<Coordinate.NegativeSpace> availableMoves) {
		for (Map.Entry<Coordinate.NegativeSpace, GraphicalEntity.NegativeSpace> entities : model.getGrid().entrySet())
			entities.getValue().draw(batch);
		model.highwayman.draw(batch);

		if (model.roadCandidate != null) {
			GraphicalEntity.NegativeSpace highlightRoad;
			if (!availableMoves.contains(model.roadCandidate))
				highlightRoad = redHighlightRoad;
			else
				highlightRoad = greenHighlightRoad;
			highlightRoad.position = model.roadCandidate;
			highlightRoad.draw(batch);
		}
		if (model.metroCandidate != null) {
			GraphicalEntity.NegativeSpace highlightMetro;
			if (!availableMoves.contains(model.metroCandidate))
				highlightMetro = redHighlightMetro;
			else
				highlightMetro = greenHighlightMetro;
			highlightMetro.position = model.metroCandidate;
			highlightMetro.draw(batch);
		}
		if (model.villageCandidate != null) {
			GraphicalEntity.NegativeSpace highlightVillage;
			if (!availableMoves.contains(model.villageCandidate))
				highlightVillage = redHighlightVillage;
			else
				highlightVillage = greenHighlightVillage;
			highlightVillage.position = model.villageCandidate;
			highlightVillage.draw(batch);
		}
		if (model.highwaymanCandidate != null) {
			GraphicalEntity.PositiveSpace highlightHighwayman;
			if (model.highwayman.position.equals(model.highwaymanCandidate))
				highlightHighwayman = redHighlightHighwayman;
			else
				highlightHighwayman = greenHighlightHighwayman;
			highlightHighwayman.position = model.highwaymanCandidate;
			highlightHighwayman.draw(batch);
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
		model.loupe.hidden = (subScene != null);
		model.loupe.update(tDelta);
		if (!model.loupe.hidden) {
			if (fogTransparency != 0.5f) {
				if (!isUpdatingFog) {
					ContinuousRendererUtil.instance.startContinuousRender();
					isUpdatingFog = true;
				} else {
					fogTransparency = Math.min(0.5f, fogTransparency + 3f * tDelta);
				}
			} else {
				if (isUpdatingFog) {
					ContinuousRendererUtil.instance.endContinuousRender();
					isUpdatingFog = false;
				}
			}
		} else {
			if (fogTransparency != 0f) {
				if (!isUpdatingFog) {
					ContinuousRendererUtil.instance.startContinuousRender();
					isUpdatingFog = true;
				} else {
					fogTransparency = Math.max(0f, fogTransparency - 3f * tDelta);
				}
			} else {
				if (isUpdatingFog) {
					ContinuousRendererUtil.instance.endContinuousRender();
					isUpdatingFog = false;
				}
			}
		}
		model.controller.hidden = (subScene != null);
		model.controller.update(tDelta);
		PlayerAction move;
		for (int i = 0; i < WorldModel.NUM_PLAYERS; i++)
			while ((move = model.getPlayer(i).getNextMove()) != null)
				move.update(tDelta);
		for (GraphicalEntity ent : model.getGrid().values())
			ent.update(tDelta);

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

	private void draw(SpriteBatch batch, Matrix4 transform, int viewportX, int viewportY, int viewportWidth, int viewportHeight, int outerStencil) {
		//clear the canvas
		shapeRenderer.setProjectionMatrix(transform);
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

		staticTiles.setProjectionMatrix(transform);
		staticTiles.begin();
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl20.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);
		staticTiles.draw(staticTilesCacheId);
		staticTiles.end();

		if (DEBUG_MODE != NO_DEBUG) {
			shapeRenderer.setProjectionMatrix(transform);
			shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
			Gdx.gl20.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);
			shapeRenderer.setColor(0, 0, 0, 1);
			//outline vertices
			if ((DEBUG_MODE & DEBUG_VERTICES) != 0)
				for (int x = 0, offsetX = model.tileWidth / 2, offsetY = model.tileHeight / 2; x <= model.mapBoundsColumns; x++, offsetX += model.tileWidth / 4 * 3, offsetY -= model.tileHeight / 2)
					for (int y = 0; y <= model.mapBoundsRows + 1; y++)
						shapeRenderer.triangle(
							offsetX,						offsetY + model.tileHeight * y,
							offsetX,						offsetY + model.tileHeight * y + model.tileHeight,
							offsetX - model.tileWidth / 4 * 3,	offsetY + model.tileHeight * y + model.tileHeight / 2
						);
						//shapeRenderer.triangle(offsetX, offsetY + model.tileHeight * y + model.tileHeight / 2, offsetX, offsetY + model.tileHeight * y - model.tileHeight / 2, offsetX + model.tileWidth / 4 * 3, offsetY + model.tileHeight * y);
			//outline edges
			if ((DEBUG_MODE & DEBUG_EDGES) != 0) {
				for (int x = 0, offsetX = -model.tileWidth / 4 * 3, offsetY = model.tileHeight + model.tileHeight / 2; x <= model.mapBoundsColumns; x++, offsetX += model.tileWidth / 4 * 3, offsetY -= model.tileHeight / 2) {
					for (int y = 0; y <= model.mapBoundsRows; y++) {
						shapeRenderer.polygon(new float[] {
							offsetX + model.tileWidth,							offsetY + model.tileHeight * y - (model.tileHeight + 1) / 2,
							offsetX + model.tileWidth / 2,						offsetY + model.tileHeight * y - (model.tileHeight + 1) / 2,
							offsetX + model.tileWidth / 4 * 3,					offsetY + model.tileHeight * y,
							offsetX + model.tileWidth / 4 * 3 + model.tileWidth / 2,	offsetY + model.tileHeight * y
						});
						shapeRenderer.polygon(new float[] {
							offsetX + model.tileWidth / 4 * 3,					offsetY + model.tileHeight * y,
							offsetX + model.tileWidth / 2,						offsetY + model.tileHeight * y + model.tileHeight / 2,
							offsetX + model.tileWidth,							offsetY + model.tileHeight * y + model.tileHeight / 2,
							offsetX + model.tileWidth + model.tileWidth / 4,			offsetY + model.tileHeight * y
						});
					}
				}
			}
			shapeRenderer.end();

			String message;
			BitmapFont fnt = model.parent.assets.get("fonts/buttons.fnt", BitmapFont.class);
			fnt.setColor(0, 0, 0, 1);
			TextBounds bnds;
			batch.setProjectionMatrix(transform);
			batch.begin();
			Gdx.gl20.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);
			//print hexagonal tile coordinates
			if ((DEBUG_MODE & DEBUG_TILES) != 0) {
				for (int x = 0, offsetX = 0, offsetY = model.tileHeight; x < model.mapBoundsColumns; x++, offsetX += model.tileWidth / 4 * 3, offsetY -= model.tileHeight / 2) {
					for (int y = 0; y < model.mapBoundsRows; y++) {
						message = "(" + x + ", " + y + ", " + (-x - y) + ")";
						bnds = fnt.getBounds(message);
						fnt.draw(batch, message, offsetX + (model.tileWidth - bnds.width) / 2, offsetY + model.tileHeight * (y + 1) - model.tileHeight / 2 + bnds.height / 2);
					}
				}
			}
			//print vertex coordinates
			if ((DEBUG_MODE & DEBUG_VERTICES) != 0) {
				for (int x = 0, offsetX = model.tileWidth / 2 - model.tileWidth / 4 * 3, offsetY = model.tileHeight / 2; x <= model.mapBoundsColumns; x++, offsetX += model.tileWidth / 4 * 3, offsetY -= model.tileHeight / 2) {
					for (int y = 0; y <= model.mapBoundsRows + 1; y++) {
						message = "(" + x + "," + y + ".5)";
						bnds = fnt.getBounds(message);
						fnt.draw(batch, message, offsetX, offsetY + model.tileHeight * (y + 1) + bnds.height / 2);
						message = "(" + (x - 1) + "," + y + ".0)";
						bnds = fnt.getBounds(message);
						fnt.draw(batch, message, offsetX - model.tileWidth / 4 * 3, offsetY + model.tileHeight * (y + 1) + bnds.height / 2);
					}
				}
			}
			batch.end();
		}

		Set<Coordinate.NegativeSpace> availableMoves = model.getPlayer(model.getCurrentPlayerTurn()).availableMoves;
		batch.setProjectionMatrix(transform);
		batch.begin();
		Gdx.gl20.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);
		staticChits.draw(batch);
		drawEntities(batch, availableMoves);
		batch.end();

		//draw fog on invalid regions when selecting a region
		if (fogTransparency != 0) {
			//begin positioning and shaping the fog
			if (outerStencil == 0)
				Gdx.gl20.glEnable(GL20.GL_STENCIL_TEST);
			//enable stencil drawing and disable graphics drawing
			Gdx.gl20.glStencilMask(0xFF);
			Gdx.gl20.glDepthMask(false);
			Gdx.gl20.glColorMask(false, false, false, false);
			if (outerStencil == 0)
				Gdx.gl20.glClear(GL20.GL_STENCIL_BUFFER_BIT);
			//if (outerStencil == 0b01) then
			//	if (0b11 & 0b01 == existing_stencil_buffer_val & 0b01) then
			//		put 0b11 on stencil buffer at pixel
			//	i.e. if existing_stencil_buffer_val at pixel is 1, then set it to 3
			//else if (outerStencil == 0b00) then
			//	if (0b10 & 0b00 == existing_stencil_buffer_val & 0b00) then
			//		put 0b10 on stencil buffer at pixel
			//	i.e. set value at pixel to 2 regardless of existing_stencil_buffer_val
			Gdx.gl20.glStencilFunc(GL20.GL_NOTEQUAL, 0x02 | outerStencil, outerStencil);
			Gdx.gl20.glStencilOp(GL20.GL_REPLACE, GL20.GL_KEEP, GL20.GL_KEEP);
			batch.setProjectionMatrix(transform);
			batch.begin();
			//FIXME: nothing works. texture where alpha == 0 still writes 2 to depth buffer
			//Gdx.gl20.glEnable(GL20.GL_BLEND);
			//Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			Gdx.gl20.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);
			batch.setShader(model.parent.assets.get("shaders/vertex/spritebatch_default.vert+shaders/fragment/spritebatch_alphatest.frag", ShaderProgram.class));
			for (Coordinate.NegativeSpace coord : availableMoves) {
				if (coord.getType() == Coordinate.Type.EDGE) {
					greenHighlightRoad.position = coord;
					greenHighlightRoad.draw(batch);
				} else {
					greenHighlightVillage.position = coord;
					greenHighlightVillage.draw(batch);
				}
			}
			batch.setShader(null);
			batch.end();
			//enable graphics drawing and disable stencil drawing
			Gdx.gl20.glColorMask(true, true, true, true);
			Gdx.gl20.glDepthMask(true);
			Gdx.gl20.glStencilMask(0x00);
			//end positioning and shaping fog position
	
			//begin drawing inside the fog
			//if (outerStencil == 0b01) then
			//	if (0b01 & 0b11 == existing_stencil_buffer_val & 0b11) then
			//		draw graphics at pixel
			//	i.e. if existing_stencil_buffer_val at pixel is 1, then draw it
			//else if (outerStencil == 0b00) then
			//	if (0b00 & 0b10 == existing_stencil_buffer_val & 0b10) then
			//		draw graphics at pixel
			//	i.e. if existing_stencil_buffer_val at pixel is 0, then draw it
			Gdx.gl20.glStencilFunc(GL20.GL_EQUAL, outerStencil, 0x02 | outerStencil);
			shapeRenderer.setProjectionMatrix(transform);
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			Gdx.gl20.glEnable(GL20.GL_BLEND);
			Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			Gdx.gl20.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);
			shapeRenderer.setColor(0, 0, 0, fogTransparency);
			shapeRenderer.rect(
				-model.getCamera().viewportWidth * (model.getCamera().zoom - 2) / 2,
				//(-model.getCamera().viewportHeight * (model.getCamera().zoom - 2) / 2) + (model.getCamera().viewportHeight * model.getCamera().zoom) - (model.getCamera().position.y + Constants.HEIGHT - model.getCamera().viewportHeight),
				model.getCamera().viewportHeight * model.getCamera().zoom / 2 - model.getCamera().position.y + Constants.HEIGHT,
				model.getCamera().viewportWidth * model.getCamera().zoom,
				-model.getCamera().viewportHeight * model.getCamera().zoom
			);
			shapeRenderer.end();
			if (outerStencil == 0)
				Gdx.gl20.glDisable(GL20.GL_STENCIL_TEST);
			else
				Gdx.gl20.glStencilFunc(GL20.GL_EQUAL, outerStencil, outerStencil);
			//end drawing inside the fog
		}
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

		/*int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();
		FrameBuffer fbo = new FrameBuffer(Format.RGBA8888, width, height, false);
		TextureRegion fboRegion = new TextureRegion(fbo.getColorBufferTexture());
		fboRegion.flip(false, true);
		fbo.begin();
		draw(batch, new Matrix4(), 0, 0, Constants.WIDTH, Constants.HEIGHT);
		fbo.end();
		batch.setTransformMatrix(model.getCamera().combined);
		batch.begin();
		Gdx.gl20.glViewport(model.getViewportX(), model.getViewportY(), model.getViewportWidth(), model.getViewportHeight());
		batch.draw(fboRegion, 0, 0, Constants.WIDTH * 2, Constants.HEIGHT * 2);
		batch.end();*/

		//draw on the canvas
		draw(batch, model.getCamera().combined, model.getViewportX(), model.getViewportY(), model.getViewportWidth(), model.getViewportHeight(), 0x00);

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
			//enable stencil drawing and disable graphics drawing
			Gdx.gl20.glStencilMask(0xFF);
			Gdx.gl20.glDepthMask(false);
			Gdx.gl20.glColorMask(false, false, false, false);
			Gdx.gl20.glClear(GL20.GL_STENCIL_BUFFER_BIT);
			//if (true) then
			//	put 0b01 on stencil buffer at pixel
			//i.e. set value at pixel to 1 regardless of existing_stencil_buffer_val
			Gdx.gl20.glStencilFunc(GL20.GL_NEVER, 1, 0);
			Gdx.gl20.glStencilOp(GL20.GL_REPLACE, GL20.GL_KEEP, GL20.GL_KEEP);
			//ShapeRenderer has problems with Fill when using the default y-up coordinate system
			shapeRenderer.setProjectionMatrix(ScaleDisplay.Y_DOWN_FULL_SCREEN_DISPLAY.getCamera().combined);
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			Gdx.gl20.glViewport(ScaleDisplay.Y_DOWN_FULL_SCREEN_DISPLAY.getViewportX(), ScaleDisplay.Y_DOWN_FULL_SCREEN_DISPLAY.getViewportY(), ScaleDisplay.Y_DOWN_FULL_SCREEN_DISPLAY.getViewportWidth(), ScaleDisplay.Y_DOWN_FULL_SCREEN_DISPLAY.getViewportHeight());
			shapeRenderer.setColor(0, 0, 0, 1);
			shapeRenderer.circle(yDownCursor.x, yDownCursor.y - Loupe.RADIUS - Loupe.STEM_HEIGHT, Loupe.RADIUS);
			shapeRenderer.end();
			//enable graphics drawing and disable stencil drawing
			Gdx.gl20.glColorMask(true, true, true, true);
			Gdx.gl20.glDepthMask(true);
			Gdx.gl20.glStencilMask(0x00);
			//end positioning and shaping loupe position

			//begin drawing inside the loupe
			//if (0b01 & 0b01 == existing_stencil_buffer_val & 0b01) then
			//	draw graphics at pixel
			//i.e. if existing_stencil_buffer_val at pixel is 1 or 3, then draw it
			Gdx.gl20.glStencilFunc(GL20.GL_EQUAL, 1, 1);
			draw(batch, model.loupe.getCamera().combined, model.loupe.getViewportX(), model.loupe.getViewportY(), model.loupe.getViewportWidth(), model.loupe.getViewportHeight(), 1);
			Vector3 cursor = model.parent.controller.getCursor(model);
			batch.setProjectionMatrix(model.loupe.getCamera().combined);
			batch.begin();
			Sprite hand = model.parent.sprites.get("cursor/select");
			hand.setBounds(cursor.x - 18, cursor.y - 64, 50, 64);
			hand.draw(batch);
			hand.setColor(0.5f, 0, 0.5f, 0.8f);
			batch.end();
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
		Gdx.input.setCursorCatched(false);
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
		Gdx.input.setCursorCatched(scene == null);

		this.subScene = scene;
	}
}
