package net.pjtb.celdroids.client.world;

import net.pjtb.celdroids.Constants;
import net.pjtb.celdroids.client.Button;
import net.pjtb.celdroids.client.Model;

import com.badlogic.gdx.graphics.OrthographicCamera;

public class WorldModel {
	public static final int MAP_VIEW_COLUMNS = 16, MAP_VIEW_ROWS = 12, TILE_SIZE = 60, MAP_VIEW_WIDTH = MAP_VIEW_COLUMNS * TILE_SIZE, CONTROL_VIEW_WIDTH = Constants.WIDTH - MAP_VIEW_WIDTH;

	public final Model parent;
	public final DirectionalPad dpad;
	public final Avatar avatar;

	public int mapBoundsColumns, mapBoundsRows;
	public final Entity[][] grid;

	private final Runnable BATTLE_BUTTON;
	public final Button actionButton;

	public final OrthographicCamera cam;

	public WorldModel(Model model) {
		this.parent = model;
		dpad = new DirectionalPad(model);
		avatar = new Avatar(this, 7, 6);

		mapBoundsColumns = MAP_VIEW_COLUMNS;
		mapBoundsRows = MAP_VIEW_ROWS;
		grid = new Entity[mapBoundsRows][mapBoundsColumns];
		grid[6][7] = avatar;
		grid[5][7] = new NonplayableCharacter();

		BATTLE_BUTTON = new Runnable() {
			@Override
			public void run() {
				parent.scene.swappedOut(true);
				parent.scene = parent.scenes.get(Model.SceneType.BATTLE);
				parent.scene.swappedIn(false);
			}
		};
		actionButton = new Button(model, null, null, 10, 296, 256, 128, "ui/button/regular", "ui/button/pressed", 255, 255, 255, 127, 255, 0, 0, 127);

		cam = new OrthographicCamera(Constants.WIDTH, Constants.HEIGHT);
	}

	public Entity getEntity(Coordinate loc) {
		if (loc.row < 0 || loc.col < 0 || loc.row >= mapBoundsRows || loc.col >= mapBoundsColumns)
			return null;
		return grid[loc.row][loc.col];
	}

	public void updateActionButtonBehavior() {
		Entity ent;
		if (avatar.isStationary() && (ent = getEntity(avatar.getNextLocation())) != null && ent instanceof NonplayableCharacter) {
			//TrainerProperties loads from XML/JSON for NPCs, socket messages for multiplayer
			//TrainerProperties { trainerName, party[], appearance }
			//parent.battleModel.properties = new net.pjtb.celdroids.client.TrainerProperties();
			actionButton.text = "Battle!";
			actionButton.callback = BATTLE_BUTTON;
		} else {
			actionButton.text = null;
		}
	}
}
