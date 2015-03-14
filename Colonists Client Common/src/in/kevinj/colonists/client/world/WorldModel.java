package in.kevinj.colonists.client.world;

import in.kevinj.colonists.Constants;
import in.kevinj.colonists.client.Button;
import in.kevinj.colonists.client.Model;
import in.kevinj.colonists.client.ScaleDisplay;
import in.kevinj.colonists.client.TrainerProperties;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Vector3;

public class WorldModel extends ScaleDisplay {
	public static class TileCoordinate implements Comparable<TileCoordinate> {
		public byte x, y;

		public TileCoordinate(int x, int y) {
			this.x = (byte) x;
			this.y = (byte) y;
		}

		public byte getZ() {
			return (byte) (-x - y);
		}

		@Override
		public int compareTo(TileCoordinate other) {
			return this.hashCode() - other.hashCode();
		}

		@Override
		public boolean equals(Object b) {
			if (b instanceof TileCoordinate) {
				TileCoordinate other = (TileCoordinate) b;
				return other.x == this.x && other.y == this.y;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return (this.x & 0xFF) << 8 | this.y & 0xFF;
		}

		@Override
		public String toString() {
			return "TileCoordinate[" + x + "," + y + "," + getZ() + "]";
		}
	}

	/**
	 * Takes advantage of the fact that regular triangles are the dual
	 * polyhedron of regular hexagons. We use two triangles stacked on top of
	 * each other for each x, y in the hexagonal grid to make it easier to
	 * find adjacent resource tiles. Vertices are referenced by the coordinates
	 * of the triangle. Edges are referenced by the midpoint of the two
	 * vertices they connect.
	 */
	public static class EntityCoordinate implements Comparable<EntityCoordinate> {
		private static final DecimalFormat FMT = new DecimalFormat("0.00");

		public final byte x, y;
		/**
		 * 0 corresponds to leftmost vertex, 1 corresponds to bottom left edge,
		 * 2 corresponds to bottom left vertex, and so on...
		 */
		public final byte xHundredths, yHundredths;

		public EntityCoordinate(int x, int xHundredths, int y, int yHundredths) {
			byte[] xNorm = normalize(x, xHundredths);
			byte[] yNorm = normalize(y, yHundredths);
			this.x = xNorm[0];
			this.xHundredths = xNorm[1];
			this.y = yNorm[0];
			this.yHundredths = yNorm[1];
		}

		private byte[] normalize(int a, int aHundredths) {
			if (aHundredths >= 0 && aHundredths < 100)
				return new byte[] { (byte) a, (byte) aHundredths };

			//make sure aHundreths is not negative
			a -= 1;
			aHundredths += 100;
			//integer division and modulus
			a += aHundredths / 100;
			aHundredths %= 100;
			return new byte[] { (byte) a, (byte) aHundredths };
		}

		public boolean isEdge() {
			return xHundredths == 50 || yHundredths == 25 || yHundredths == 75;
		}

		public List<TileCoordinate> adjacentTiles() {
			List<TileCoordinate> list = new ArrayList<TileCoordinate>(3);
			list.add(new TileCoordinate(x, (y * 100 + xHundredths - 50) / 100));
			list.add(new TileCoordinate(x - 1, y - 1));
			list.add(new TileCoordinate((x * 100 - xHundredths) / 100, y));
			return list;
		}

		public List<EntityCoordinate> adjacentEdges() {
			List<EntityCoordinate> list;
			if (isEdge()) {
				list = new ArrayList<EntityCoordinate>(4);
				//up [yHundredths == 25]/down [yHundredths == 75] left
				list.add(new EntityCoordinate(x, xHundredths - 50, y, 25));
				//up right [yHundredths == 25]/left [yHundredths == 75]
				list.add(new EntityCoordinate(x, 2 * xHundredths, y, yHundredths + 50));
				//down [yHundredths == 25]/up [yHundredths == 75] right
				list.add(new EntityCoordinate(x, xHundredths + 50, y, 2 * yHundredths - 25));
				//down left [yHundredths == 25]/right [yHundredths == 75]
				list.add(new EntityCoordinate(x, 0, y, yHundredths - 50));
			} else {
				list = new ArrayList<EntityCoordinate>(3);
				int right = -(int) Math.signum(yHundredths - 25);
				//down left [yHundredths == 0]/right [yHundredths == 50]
				list.add(new EntityCoordinate(x, 0, y, yHundredths - 25));
				//right [yHundredths == 0]/left [yHundredths == 50]
				list.add(new EntityCoordinate(x, xHundredths + 50 * right, y, yHundredths + 25 * right));
				//up left [yHundredths == 0]/right [yHundredths == 50]
				list.add(new EntityCoordinate(x, xHundredths, y, yHundredths + 25));
			}
			return list;
		}

		public List<EntityCoordinate> adjacentVertices() {
			List<EntityCoordinate> list;
			if (isEdge()) {
				list = new ArrayList<EntityCoordinate>(2);
				list.add(new EntityCoordinate(x, (xHundredths - 50) / 100 * 100, y, yHundredths - 25));
				list.add(new EntityCoordinate(x, (xHundredths + 50) / 100 * 100, y, yHundredths + 25));
			} else {
				list = new ArrayList<EntityCoordinate>(3);
				int right = -(int) Math.signum(yHundredths - 25);
				//right [yHundredths == 0]/left [yHundredths == 50]
				list.add(new EntityCoordinate(x + right, xHundredths, y, yHundredths + 50 * right));
				//up
				list.add(new EntityCoordinate(x, xHundredths, y, yHundredths + 50));
				//down
				list.add(new EntityCoordinate(x, xHundredths, y, yHundredths - 50));
			}
			return list;
		}

		@Override
		public int compareTo(EntityCoordinate other) {
			return this.hashCode() - other.hashCode();
		}

		@Override
		public boolean equals(Object b) {
			if (b instanceof EntityCoordinate) {
				EntityCoordinate other = (EntityCoordinate) b;
				return other.x == this.x && other.xHundredths == this.xHundredths && other.y == this.y && other.yHundredths == this.yHundredths;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return (this.x & 0xFF) << 24 | (this.xHundredths & 0xFF) << 16 | (this.y & 0xFF) << 8 | this.yHundredths & 0xFF;
		}

		@Override
		public String toString() {
			return "EntityCoordinate[" + FMT.format(x + xHundredths / 100f) +"," + FMT.format(y + yHundredths / 100f) + "]";
		}
	}

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
	public final DirectionalPad dpad;
	public final Avatar avatar;

	public int mapBoundsColumns, mapBoundsRows;
	public final MapTile[][] resources;
	public final Map<EntityCoordinate, Entity> grid;
	public final List<Entity> animatedEntities;
	public TileCoordinate highwayman;
	public TileCoordinate highwaymanCandidate;
	public EntityCoordinate villageCandidate, metroCandidate, roadCandidate;

	private final Runnable BATTLE_BUTTON;
	public final Button actionButton;

	private int screenWidth, screenHeight;

	public WorldModel(Model model) {
		this.parent = model;
		controller = new MapInteraction(this);
		loupe = new Loupe();
		dpad = new DirectionalPad(model);
		avatar = new Avatar(this, 7, 6);

		mapBoundsColumns = MAP_VIEW_COLUMNS;
		mapBoundsRows = MAP_VIEW_ROWS;
		resources = new MapTile[mapBoundsRows][mapBoundsColumns];
		initializeMap();

		grid = new HashMap<EntityCoordinate, Entity>();
		//road tests
		grid.put(new EntityCoordinate(1, 0, 2, 75), avatar);
		grid.put(new EntityCoordinate(1, 0, 2, 25), avatar);
		grid.put(new EntityCoordinate(2, 0, 2, 75), avatar);
		grid.put(new EntityCoordinate(2, 0, 2, 25), avatar);
		grid.put(new EntityCoordinate(2, 50, 2, 25), avatar);
		grid.put(new EntityCoordinate(3, 0, 2, 25), avatar);
		grid.put(new EntityCoordinate(3, 50, 2, 25), avatar);
		grid.put(new EntityCoordinate(4, 0, 2, 25), avatar);
		grid.put(new EntityCoordinate(4, 0, 2, 75), avatar);
		//house tests
		grid.put(new EntityCoordinate(1, 0, 3, 0), avatar);
		grid.put(new EntityCoordinate(2, 0, 2, 50), avatar);
		animatedEntities = new ArrayList<Entity>();

		BATTLE_BUTTON = new Runnable() {
			@Override
			public void run() {
				parent.battleModel.initLocal(parent.assets.get(((NonplayableCharacter) getEntity(avatar.getNextLocation())).trainerProps, TrainerProperties.class).createInstance(parent.battleModel));
				parent.swapScene(parent.scenes.get(Model.SceneType.BATTLE));
			}
		};
		actionButton = new Button(model, null, null, 10, 296, 256, 128, "ui/button/regular", "ui/button/pressed", 255, 255, 255, 127, 255, 0, 0, 127);

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
					highwayman = new TileCoordinate(x, y);
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

	public MapTile getTile(TileCoordinate coord) {
		if (coord == null) return null;
		return resources[coord.y][coord.x];
	}

	public Entity getEntity(Coordinate loc) {
		//if (loc.row < 0 || loc.col < 0 || loc.row >= mapBoundsRows || loc.col >= mapBoundsColumns)
			return null;
		//return grid[loc.row][loc.col];
	}

	public void updateActionButtonBehavior() {
		if (avatar.isStationary() && getEntity(avatar.getNextLocation()) instanceof NonplayableCharacter) {
			actionButton.text = "Battle!";
			actionButton.callback = BATTLE_BUTTON;
		} else {
			actionButton.text = null;
		}
	}
}
