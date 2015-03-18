package in.kevinj.colonists.client.world;

import in.kevinj.colonists.AiPlayer;
import in.kevinj.colonists.Constants;
import in.kevinj.colonists.LocalPlayer;
import in.kevinj.colonists.NetworkPlayer;
import in.kevinj.colonists.PendingPlayer;
import in.kevinj.colonists.Player;
import in.kevinj.colonists.client.Model;
import in.kevinj.colonists.client.ScaleDisplay;
import in.kevinj.colonists.world.Coordinate;
import in.kevinj.colonists.world.GameMap;
import in.kevinj.colonists.world.MapTile;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Vector3;

public class WorldModel extends ScaleDisplay implements GameMap<GraphicalEntity.NegativeSpace> {
	public final class Loupe extends ScaleDisplay {
		public static final int RADIUS = 128;
		public static final int STEM_HEIGHT = 64;
		public static final int STEM_WIDTH = 192;

		public boolean hidden;

		protected Loupe() {
			hidden = true;
		}

		@Override
		public final void resize(int screenWidth, int screenHeight) {
			super.resize(screenWidth, screenHeight);
			cam.zoom = 1f;
			cam.position.set(Constants.WIDTH, Constants.HEIGHT / 2, 0);
			cam.update();
		}

		public void update(float tDelta) {
			if (!Gdx.input.isButtonPressed(Buttons.LEFT)) {
				hidden |= true;
				return;
			}

			hidden |= false;
			//center the loupe on the cursor
			Vector3 cursor = parent.controller.getCursor(WorldModel.this);
			cam.position.set(Constants.WIDTH, Constants.HEIGHT / 2, 0);
			cam.update();
			cam.project(cursor, 0, 0, Constants.WIDTH / 2, Constants.WIDTH * getViewportHeight() / getViewportWidth() / 2);
			float zoom = WorldModel.this.cam.zoom;
			cam.position.x = ((2 * cursor.x + (Constants.WIDTH / 2)) * (zoom - 1) + Constants.WIDTH) / zoom;
			cam.position.y = ((2 * cursor.y - (Constants.WIDTH * getViewportHeight() / getViewportWidth()) / 2) * (zoom - 1)) / zoom
				//correction against the loupe
				- RADIUS - STEM_HEIGHT
				//correction against WorldScene camera
				+ WorldModel.this.cam.position.y / 2 + Constants.HEIGHT / 4
			;
			cam.update();
		}

		@Override
		public int getViewportX() {
			return WorldModel.this.getViewportX();
		}

		@Override
		public int getViewportY() {
			return WorldModel.this.getViewportY();
		}

		@Override
		public int getViewportWidth() {
			return WorldModel.this.getViewportWidth();
		}

		@Override
		public int getViewportHeight() {
			return WorldModel.this.getViewportHeight();
		}
	}

	public static final int MAP_VIEW_COLUMNS = 7, MAP_VIEW_ROWS = 7;

	public final Model parent;
	public final MapInteraction controller;
	public final Loupe loupe;

	public int tileWidth, tileHeight, settlementRadius;
	public int mapBoundsColumns, mapBoundsRows;
	public final MapTile[][] resources;
	public final GraphicalEntity.PositiveSpace highwayman;
	public Coordinate.PositiveSpace highwaymanCandidate;
	public Coordinate.NegativeSpace villageCandidate, metroCandidate, roadCandidate;
	private final Map<Coordinate.NegativeSpace, GraphicalEntity.NegativeSpace> grid;

	private final int[] initializeGridStages;
	private final Set<Coordinate.NegativeSpace> availableVertices;

	private final Player[] players;
	public final int self;
	private int currentPlayerTurn;

	private int screenWidth, screenHeight;

	public WorldModel(Model model) {
		this.parent = model;
		controller = new MapInteraction(this);
		loupe = new Loupe();

		mapBoundsColumns = MAP_VIEW_COLUMNS;
		mapBoundsRows = MAP_VIEW_ROWS;
		resources = new MapTile[mapBoundsRows][mapBoundsColumns];
		highwayman = new GraphicalEntity.Highwayman(this);
		GameMap.Helper.initializeMap(resources, highwayman);

		grid = new HashMap<Coordinate.NegativeSpace, GraphicalEntity.NegativeSpace>();
		availableVertices = Coordinate.NegativeSpace.allVertices();

		players = new Player[NUM_PLAYERS];
		initializeGridStages = new int[NUM_PLAYERS];
		self = 0;
		for (int i = 0; i < NUM_PLAYERS; initializeGridStages[i]++, i++)
			if (i == self)
				players[i] = new LocalPlayer(null, new HashSet<Coordinate.NegativeSpace>(availableVertices));
			else
				players[i] = new PendingPlayer(null, new HashSet<Coordinate.NegativeSpace>(availableVertices));
		//house tests
//		addToGrid(Coordinate.NegativeSpace.valueOf(1, 0, 3, 0), new GraphicalEntity.Metro(this, currentPlayerTurn));
//		addToGrid(Coordinate.NegativeSpace.valueOf(1, 0, 2, 50), new GraphicalEntity.Metro(this, currentPlayerTurn));
//		addToGrid(Coordinate.NegativeSpace.valueOf(2, 0, 2, 50), new GraphicalEntity.Metro(this, currentPlayerTurn));
		//road tests
//		addToGrid(Coordinate.NegativeSpace.valueOf(1, 0, 2, 75), new GraphicalEntity.Road(this, currentPlayerTurn));
//		addToGrid(Coordinate.NegativeSpace.valueOf(2, 0, 2, 25), new GraphicalEntity.Road(this, currentPlayerTurn));

		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	@Override
	public final void resize(int screenWidth, int screenHeight) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		super.resize(screenWidth, screenHeight);
		cam.zoom = 2f;
		cam.position.set(Constants.WIDTH, Constants.HEIGHT / 2, 0);
		cam.update();

		loupe.resize(screenWidth, screenHeight);
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	private void init() {
		
	}

	public void initLocal(AiPlayer op) {
		init();
		setPlayer(-1, op);
	}

	public void initRemote(NetworkPlayer op, boolean swapTurnsAtEnd) {
		init();
		setPlayer(-1, op);
	}

	@Override
	public Map<Coordinate.NegativeSpace, GraphicalEntity.NegativeSpace> getGrid() {
		return Collections.unmodifiableMap(grid);
	}

	@Override
	public GraphicalEntity.NegativeSpace addToGrid(Coordinate.NegativeSpace loc, GraphicalEntity.NegativeSpace ent) {
		GraphicalEntity.NegativeSpace old = grid.put(loc, ent);
		if (old != null)
			old.setPosition(null);
		ent.setPosition(loc);
		if (GameMap.Helper.initialAddToGrid(this, loc, ent, initializeGridStages[ent.getPlayer()], availableVertices))
			initializeGridStages[ent.getPlayer()]++;
		else
			GameMap.Helper.incrementalUpdateAfterAddToGrid(this, loc, ent);
		return old;
	}

	@Override
	public GraphicalEntity removeFromGrid(Coordinate.NegativeSpace loc) {
		GraphicalEntity.NegativeSpace old = grid.remove(loc);
		if (old == null)
			return null; //no changes made
		else
			old.setPosition(null);
		GameMap.Helper.incrementalUpdateAfterRemoveFromGrid(this, loc, old);
		return old;
	}

	public MapTile getTile(Coordinate.PositiveSpace coord) {
		if (coord == null) return null;
		return resources[coord.y][coord.x];
	}

	@Override
	public Player getPlayer(int i) {
		return players[i];
	}

	public void setPlayer(int i, Player newP) {
		Player oldP = players[i];
		if (oldP instanceof PendingPlayer)
			((PendingPlayer) oldP).transferTo(newP);
		players[i] = newP;
	}

	@Override
	public int getCurrentPlayerTurn() {
		return currentPlayerTurn;
	}

	@Override
	public GraphicalEntity.PositiveSpace getHighwayman() {
		return highwayman;
	}

	@Override
	public void setHighwaymanCandidate(Coordinate.PositiveSpace coord) {
		highwaymanCandidate = coord;
	}

	@Override
	public void setRoadCandidate(Coordinate.NegativeSpace coord) {
		roadCandidate = coord;
	}

	@Override
	public void setMetroCandidate(Coordinate.NegativeSpace coord) {
		metroCandidate = coord;
	}

	@Override
	public void setVillageCandidate(Coordinate.NegativeSpace coord) {
		villageCandidate = coord;
	}
}
