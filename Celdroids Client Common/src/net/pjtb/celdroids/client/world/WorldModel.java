package net.pjtb.celdroids.client.world;

import net.pjtb.celdroids.client.Button;
import net.pjtb.celdroids.client.Model;

public class WorldModel {
	public static final int WIDTH = 16, HEIGHT = 12;

	public final Model parent;
	public final DirectionalPad dpad;
	public final Avatar avatar;

	public final Entity[][] grid;

	private final Runnable BATTLE_BUTTON;
	public final Button actionButton;

	public WorldModel(Model model) {
		this.parent = model;
		dpad = new DirectionalPad(model);
		avatar = new Avatar(this);

		grid = new Entity[HEIGHT][WIDTH];
		grid[0][0] = avatar;
		grid[0][1] = new NonplayableCharacter();

		BATTLE_BUTTON = new Runnable() {
			@Override
			public void run() {
				parent.scene.swappedOut(true);
				parent.scene = parent.scenes.get(Model.SceneType.BATTLE);
				parent.scene.swappedIn(false);
			}
		};
		actionButton = new Button(model, null, null, 10, 296, 256, 128, "ui/button/regular", "ui/button/pressed", 255, 255, 255, 127);
	}

	public Entity getEntity(Coordinate loc) {
		if (loc.row < 0 || loc.col < 0 || loc.row >= HEIGHT || loc.col >= WIDTH)
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
