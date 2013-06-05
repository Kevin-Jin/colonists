package net.pjtb.celdroids.client;

import java.util.ArrayList;

public class CeldroidProperties {
	public final String name;
	public final String sprite;
	public final ArrayList<CeldroidBattleMove> moves;

	/**
	 * Private constructor can still be called final variables can still be
	 * assigned by reflection in Json class.
	 * Just set values to garbage.
	 */
	private CeldroidProperties() {
		name = null;
		sprite = null;
		moves = null;
	}
}
