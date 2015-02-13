package in.kevinj.colonists.client.world;

import in.kevinj.colonists.Constants;
import in.kevinj.colonists.client.Button;
import in.kevinj.colonists.client.Model;
import in.kevinj.colonists.client.ScaleDisplay;
import in.kevinj.colonists.client.TrainerProperties;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import com.badlogic.gdx.Gdx;

public class WorldModel extends ScaleDisplay {
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

	private int screenWidth, screenHeight;

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
