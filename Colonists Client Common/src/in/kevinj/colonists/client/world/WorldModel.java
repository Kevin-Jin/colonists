package in.kevinj.colonists.client.world;

import in.kevinj.colonists.Constants;
import in.kevinj.colonists.client.GraphUtil;
import in.kevinj.colonists.client.Model;
import in.kevinj.colonists.client.ScaleDisplay;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
	//TODO: this should be per-player
	private final Set<Coordinate.NegativeSpace> availableMoves;

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
		availableMoves = availableMovesCleanUpdate();
		//road tests
		addToGrid(Coordinate.NegativeSpace.valueOf(1, 0, 2, 75), new Entity.Road(this, 0));
		//house tests
		addToGrid(Coordinate.NegativeSpace.valueOf(1, 0, 3, 0), new Entity.Metro(this, 0));

		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	private Set<Coordinate.NegativeSpace> availableMovesCleanUpdate() {
		//TODO: keep separate records for each player's roads and settlements
		//outside of grid so that we don't need to waste time here
		Set<Coordinate.NegativeSpace> currentPlayerSettlements = new HashSet<Coordinate.NegativeSpace>();
		Set<Coordinate.NegativeSpace> currentPlayerRoads = new HashSet<Coordinate.NegativeSpace>();
		Set<Coordinate.NegativeSpace> otherPlayerEnts = new HashSet<Coordinate.NegativeSpace>();
		for (Map.Entry<Coordinate.NegativeSpace, Entity.NegativeSpace> entry: grid.entrySet()) {
			//TODO: check player that entity belongs to -> otherPlayerEnts
			Coordinate.NegativeSpace coord = entry.getKey();
			if (coord.isEdge())
				currentPlayerRoads.add(coord);
			else
				currentPlayerSettlements.add(coord);
		}
		currentPlayerSettlements = Collections.unmodifiableSet(currentPlayerSettlements);
		currentPlayerRoads = Collections.unmodifiableSet(currentPlayerRoads);
		otherPlayerEnts = Collections.unmodifiableSet(otherPlayerEnts);
		return GraphUtil.dfsForAvailable(currentPlayerSettlements, currentPlayerRoads, otherPlayerEnts);
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

	public Map<Coordinate.NegativeSpace, Entity.NegativeSpace> getGrid() {
		return Collections.unmodifiableMap(grid);
	}

	public Entity addToGrid(Coordinate.NegativeSpace loc, Entity.NegativeSpace ent) {
		Entity.NegativeSpace old = grid.put(loc, ent);
		if (old != null)
			old.position = null;
		ent.position = loc;

		//incremental update available moves
		availableMoves.remove(loc);
		if (loc.isEdge()) {
			Coordinate.NegativeSpace[] vertices = Coordinate.NegativeSpace.vertices(loc);
			for (Coordinate.NegativeSpace neighbor : vertices)
				if (!grid.containsKey(neighbor) && neighbor.inBounds())
					availableMoves.add(neighbor);
		} else {
			for (Coordinate.NegativeSpace edge : loc.adjacentEdges())
				if (!grid.containsKey(edge) && edge.inBounds())
					availableMoves.add(edge);
		}
		assert availableMoves.equals(availableMovesCleanUpdate());
		return old;
	}

	public Entity removeFromGrid(Coordinate.NegativeSpace loc) {
		Entity.NegativeSpace old = grid.remove(loc);
		if (old == null)
			return null; //no changes made
		else
			old.position = null;

		//incremental update available moves
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
					availableMoves.remove(neighbor);
			}
			if (connected)
				availableMoves.add(loc);
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
					availableMoves.remove(edge);
			}
			if (connected)
				availableMoves.add(loc);
		}
		assert availableMoves.equals(availableMovesCleanUpdate());
		return old;
	}

	public MapTile getTile(Coordinate.PositiveSpace coord) {
		if (coord == null) return null;
		return resources[coord.y][coord.x];
	}

	public Set<Coordinate.NegativeSpace> getAvailableMoves() {
		return Collections.unmodifiableSet(availableMoves);
	}
}
