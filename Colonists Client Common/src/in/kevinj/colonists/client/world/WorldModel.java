package in.kevinj.colonists.client.world;

import in.kevinj.colonists.Constants;
import in.kevinj.colonists.client.Button;
import in.kevinj.colonists.client.Model;
import in.kevinj.colonists.client.TrainerProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import com.badlogic.gdx.graphics.OrthographicCamera;

public class WorldModel {
	public static final int MAP_VIEW_COLUMNS = 7, MAP_VIEW_ROWS = 7;

	public final Model parent;
	public final DirectionalPad dpad;
	public final Avatar avatar;

	public int mapBoundsColumns, mapBoundsRows;
	public final MapTile[][] resources;
	public final Entity[][] grid;
	public final List<Entity> animatedEntities;

	private final Runnable BATTLE_BUTTON;
	public final Button actionButton;

	public final OrthographicCamera cam;

	public WorldModel(Model model) {
		this.parent = model;
		dpad = new DirectionalPad(model);
		avatar = new Avatar(this, 7, 6);

		mapBoundsColumns = MAP_VIEW_COLUMNS;
		mapBoundsRows = MAP_VIEW_ROWS;
		resources = new MapTile[mapBoundsRows][mapBoundsColumns];
		initializeMap();

		grid = new Entity[mapBoundsRows][mapBoundsColumns];
		//grid[5][7] = new NonplayableCharacter(this, 7, 5, "trainers/red.json");
		animatedEntities = new ArrayList<Entity>();
		//animatedEntities.add(avatar);
		//animatedEntities.add(grid[5][7]);

		BATTLE_BUTTON = new Runnable() {
			@Override
			public void run() {
				parent.scene.swappedOut(true);
				parent.scene = parent.scenes.get(Model.SceneType.BATTLE);
				parent.battleModel.initLocal(parent.assets.get(((NonplayableCharacter) getEntity(avatar.getNextLocation())).trainerProps, TrainerProperties.class).createInstance(parent.battleModel));
				parent.scene.swappedIn(true);
			}
		};
		actionButton = new Button(model, null, null, 10, 296, 256, 128, "ui/button/regular", "ui/button/pressed", 255, 255, 255, 127, 255, 0, 0, 127);

		cam = new OrthographicCamera();
		cam.setToOrtho(false, Constants.WIDTH, Constants.HEIGHT);
		cam.zoom = 2f;
		cam.translate(Constants.WIDTH / 2, 0);
		cam.update();
	}

	private int randomOrientation(Random r, int a, int b) {
		if (r.nextBoolean())
			return a;
		else
			return b;
	}

	private void initializeMap() {
		Random r = new Random();
		boolean placePort = false;
		//TODO: algorithm to randomly stagger the 9 port tiles with no two ports rotated to two edges adjacent to one another
		int remainingStaggerings = 5;
		Queue<MapTile.ResourceTile> tiles = MapTile.ResourceTile.getRandomTiles(r);
		for (int x = 1; x < 6; x++) {
			//if (remainingStaggerings > 0 && r.nextBoolean())
				//remainingStaggerings--;
			int startTile = Math.max(x - 2, 1);
			int endTile = startTile + 5 - Math.abs(3 - x);
			for (int y = startTile; y < endTile; y++)
				resources[y][x] = tiles.poll();
			placePort = x % 2 != 0 ^ remainingStaggerings % 2 == 1;
			resources[startTile - 1][x] = new MapTile.PortTile(placePort ? MapTile.PortType.NONE : MapTile.PortType.BAMBOO, x == 3 ? 0 : x < 3 ? randomOrientation(r, 0, 300) : randomOrientation(r, 0, 60));
			placePort = x %2 == 0 ^ remainingStaggerings % 2 == 1;
			resources[endTile][x] = new MapTile.PortTile(placePort ? MapTile.PortType.NONE : MapTile.PortType.STONE, x == 3 ? 180 : x < 3 ? randomOrientation(r, 180, 240) : randomOrientation(r, 180, 120));
		}
		for (int y = 0; y < 4; y++) {
			placePort = y % 2 != 0 ^ remainingStaggerings % 2 == 1;
			resources[y][0] = new MapTile.PortTile(placePort ? MapTile.PortType.NONE : MapTile.PortType.IRON, y > 2 ? 240 : randomOrientation(r, 240, 300));
			placePort = y % 2 != 0 ^ remainingStaggerings % 2 == 1;
			resources[y + 3][6] = new MapTile.PortTile(placePort ? MapTile.PortType.NONE : MapTile.PortType.RICE, y > 2 ? 120 : randomOrientation(r, 120, 60));
		}
	}

	public Entity getEntity(Coordinate loc) {
		if (loc.row < 0 || loc.col < 0 || loc.row >= mapBoundsRows || loc.col >= mapBoundsColumns)
			return null;
		return grid[loc.row][loc.col];
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
