package in.kevinj.colonists.client.world;

import in.kevinj.colonists.Constants;
import in.kevinj.colonists.client.AiPlayer;
import in.kevinj.colonists.client.GraphUtil;
import in.kevinj.colonists.client.LocalPlayer;
import in.kevinj.colonists.client.Model;
import in.kevinj.colonists.client.NetworkPlayer;
import in.kevinj.colonists.client.PendingPlayer;
import in.kevinj.colonists.client.Player;
import in.kevinj.colonists.client.ScaleDisplay;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Vector3;

public class WorldModel extends ScaleDisplay {
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
	public final Entity.PositiveSpace highwayman;
	public Coordinate.PositiveSpace highwaymanCandidate;
	public Coordinate.NegativeSpace villageCandidate, metroCandidate, roadCandidate;
	private final Map<Coordinate.NegativeSpace, Entity.NegativeSpace> grid;

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
		highwayman = new Entity.Highwayman(this);
		initializeMap();

		grid = new HashMap<Coordinate.NegativeSpace, Entity.NegativeSpace>();
		players = new Player[4];
		self = 0;
		List<Set<Coordinate.NegativeSpace>> initialAvailableMoves = availableMovesCleanUpdate();
		for (int i = 0; i < 4; i++)
			if (i == self)
				players[i] = new LocalPlayer(null, this, initialAvailableMoves.get(i));
			else
				players[i] = new PendingPlayer(null, this, initialAvailableMoves.get(i));
		//road tests
		addToGrid(Coordinate.NegativeSpace.valueOf(1, 0, 2, 75), new Entity.Road(this, currentPlayerTurn));
		//house tests
		addToGrid(Coordinate.NegativeSpace.valueOf(1, 0, 3, 0), new Entity.Metro(this, currentPlayerTurn));

		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	private List<Set<Coordinate.NegativeSpace>> availableMovesCleanUpdate() {
		Map<Coordinate.NegativeSpace, Entity.NegativeSpace> currentPlayerSettlements = new HashMap<Coordinate.NegativeSpace, Entity.NegativeSpace>();
		Map<Coordinate.NegativeSpace, Entity.NegativeSpace> currentPlayerRoads = new HashMap<Coordinate.NegativeSpace, Entity.NegativeSpace>();
		for (Map.Entry<Coordinate.NegativeSpace, Entity.NegativeSpace> entry: grid.entrySet()) {
			if (entry.getKey().isEdge())
				currentPlayerRoads.put(entry.getKey(), entry.getValue());
			else
				currentPlayerSettlements.put(entry.getKey(), entry.getValue());
		}
		currentPlayerSettlements = Collections.unmodifiableMap(currentPlayerSettlements);
		currentPlayerRoads = Collections.unmodifiableMap(currentPlayerRoads);
		return GraphUtil.dfsForAvailable(currentPlayerSettlements, currentPlayerRoads);
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

	private void initializeMap() {
		Random r = new Random();

		//keystone water tiles only touch a resource tile on one edge (i.e.
		//corner in the hexagon that the map makes). these keystones exist at
		//(0, 0), (0, 3), (3, 0), (3, 6), (6, 3), (6, 6). pick a
		//random one as a reference point to make PortTile.getRandomPorts()
		//easier to implement.
		int keystone = r.nextInt(6);
		int x, y, rot;
		switch (keystone) {
			case 0:	x = 3;	y = 0;	rot = 0;	break;
			case 1:	x = 6;	y = 3;	rot = 60;	break;
			case 2:	x = 6;	y = 6;	rot = 120;	break;
			case 3:	x = 3;	y = 6;	rot = 180;	break;
			case 4:	x = 0;	y = 3;	rot = 240;	break;
			case 5:	x = 0;	y = 0;	rot = 300;	break;
			default:	throw new AssertionError("Wrong keystone ID");
		}
		Queue<MapTile> tiles = new LinkedList<MapTile>();
		MapTile.PortTile.getRandomPorts(r, rot, tiles);
		MapTile.ResourceTile.getRandomResources(r, tiles);
		for (int rad = 3; rad >= 0; --rad) {
			for (int i = 0; i < Math.max(1, 6 * rad); i++) {
				resources[y][x] = tiles.poll();
				if (resources[y][x].isResource() && resources[y][x].getResourceType() == MapTile.ResourceType.WASTELAND)
					highwayman.position = Coordinate.PositiveSpace.valueOf(x, y);
				if (y == 3 - rad) {
					if (x == 3) y++;
					x++;
				} else if (y == 3 + rad) {
					if (x == 3) y--;
					x--;
				} else if (x > 3) {
					if (x != 3 + rad) x++;
					y++;
				} else if (x < 3) {
					if (x != 3 - rad) x--;
					y--;
				}
			}
			if (y == 3 - rad) {
				if (x != 3) x++;
				y++;
			} else if (y == 3 + rad) {
				if (x != 3) x--;
				y--;
			} else if (x == 3 - rad) {
				x++;
			} else if (x == 3 + rad) {
				x--;
			}
		}
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

	public Map<Coordinate.NegativeSpace, Entity.NegativeSpace> getGrid() {
		return Collections.unmodifiableMap(grid);
	}

	public Entity addToGrid(Coordinate.NegativeSpace loc, Entity.NegativeSpace ent) {
		Entity.NegativeSpace old = grid.put(loc, ent);
		if (old != null)
			old.position = null;
		ent.position = loc;

		//incremental update available moves
		//TODO: update availableMoves for all players
		players[currentPlayerTurn].availableMoves.remove(loc);
		if (loc.isEdge()) {
			Coordinate.NegativeSpace[] vertices = Coordinate.NegativeSpace.vertices(loc);
			for (Coordinate.NegativeSpace neighbor : vertices)
				if (!grid.containsKey(neighbor) && neighbor.inBounds())
					players[currentPlayerTurn].availableMoves.add(neighbor);
		} else {
			for (Coordinate.NegativeSpace edge : loc.adjacentEdges())
				if (!grid.containsKey(edge) && edge.inBounds())
					players[currentPlayerTurn].availableMoves.add(edge);
		}
		assert players[currentPlayerTurn].availableMoves.equals(availableMovesCleanUpdate().get(currentPlayerTurn));
		return old;
	}

	public Entity removeFromGrid(Coordinate.NegativeSpace loc) {
		Entity.NegativeSpace old = grid.remove(loc);
		if (old == null)
			return null; //no changes made
		else
			old.position = null;

		//incremental update available moves
		//TODO: update availableMoves for all players
		if (loc.isEdge()) {
			boolean connected = false, bridge;
			for (Coordinate.NegativeSpace neighbor : Coordinate.NegativeSpace.vertices(loc)) {
				if (grid.containsKey(neighbor)) {
					connected = true; //TODO: connected = (grid.get(neighbor) is our own settlement)
					continue;
				}

				bridge = true;
				for (Coordinate.NegativeSpace otherEdge : neighbor.adjacentEdges()) {
					if (grid.containsKey(otherEdge)) { //TODO: if (grid.containsKey(otherEdge) && grid.get(otherEdge) is our own road)
						bridge = false;
						break;
					}
				}

				//if the empty vertex is not connected to any of our other edges,
				//there is no longer any way to reach the vertex
				if (bridge)
					players[currentPlayerTurn].availableMoves.remove(neighbor);
			}
			if (connected)
				players[currentPlayerTurn].availableMoves.add(loc);
		} else {
			boolean connected = false, articulationPoint;
			for (Coordinate.NegativeSpace edge : loc.adjacentEdges()) {
				if (grid.containsKey(edge)) {
					connected = true; //TODO: connected = (grid.get(edge) is our own road)
					continue;
				}

				articulationPoint = true;
				for (Coordinate.NegativeSpace otherVertex : edge.adjacentVertices()) {
					if (grid.containsKey(otherVertex)) { //TODO: if (grid.containsKey(otherVertex) && grid.get(otherVertex) is our own settlement)
						articulationPoint = false;
						break;
					}
				}

				//if the empty edge is not connected to any of our other vertices,
				//there is no longer any way to reach the edge
				if (articulationPoint)
					players[currentPlayerTurn].availableMoves.remove(edge);
			}
			if (connected)
				players[currentPlayerTurn].availableMoves.add(loc);
		}
		assert players[currentPlayerTurn].availableMoves.equals(availableMovesCleanUpdate().get(currentPlayerTurn));
		return old;
	}

	public MapTile getTile(Coordinate.PositiveSpace coord) {
		if (coord == null) return null;
		return resources[coord.y][coord.x];
	}

	public Player getPlayer(int i) {
		return players[i];
	}

	public void setPlayer(int i, Player newP) {
		Player oldP = players[i];
		if (oldP instanceof PendingPlayer)
			((PendingPlayer) oldP).transferTo(newP);
		players[i] = newP;
	}

	public int getCurrentPlayerTurn() {
		return currentPlayerTurn;
	}
}
