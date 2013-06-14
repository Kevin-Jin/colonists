package net.pjtb.celdroids.client;

import java.util.ArrayList;

import net.pjtb.celdroids.client.battle.BattleModel;

public class TrainerProperties {
	public final String trainerName;
	public final ArrayList<CeldroidProperties> monsters;

	/**
	 * Private constructor can still be called final variables can still be
	 * assigned by reflection in Json class.
	 * Just set values to garbage.
	 */
	private TrainerProperties() {
		trainerName = null;
		monsters = null;
	}

	public AiBattleOpponent createInstance(BattleModel model) {
		AiBattleOpponent op = new AiBattleOpponent(trainerName, model);
		for (CeldroidProperties monster : monsters)
			op.party.add(new CeldroidMonster(monster, null));
		return op;
	}
}
