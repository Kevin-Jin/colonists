package net.pjtb.celdroids.client.world;

import net.pjtb.celdroids.client.Button;
import net.pjtb.celdroids.client.Model;

public class WorldModel {
	public final Model parent;
	public final DirectionalPad dpad;
	public final Avatar avatar;

	private final Runnable BATTLE_BUTTON;
	public final Button actionButton;

	public WorldModel(Model model) {
		this.parent = model;
		dpad = new DirectionalPad(model);
		avatar = new Avatar(this);

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

	public void updateActionButtonBehavior() {
		/*if (avatar is standing still facing trainer in adjacent square, or is colliding with trainer) {
			//TrainerProperties loads from XML/JSON for NPCs, socket messages for multiplayer
			//TrainerProperties { trainerName, party[], appearance }
			parent.battleModel.properties = new net.pjtb.celdroids.client.TrainerProperties();
			actionButton.text = "Battle!";
			actionButton.callback = BATTLE_BUTTON;
		}*/
	}
}
