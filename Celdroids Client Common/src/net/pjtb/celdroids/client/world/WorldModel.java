package net.pjtb.celdroids.client.world;

import net.pjtb.celdroids.client.Model;

public class WorldModel {
	public final Model parent;
	public final DirectionalPad dpad;
	public final Avatar avatar;

	public WorldModel(Model model) {
		this.parent = model;
		dpad = new DirectionalPad(model);
		avatar = new Avatar(this);
	}
}
