package in.kevinj.colonists.client;

import in.kevinj.colonists.client.world.BattleModel;

public class TrainerProperties {
	public final String trainerName;

	/**
	 * Private constructor can still be called final variables can still be
	 * assigned by reflection in Json class.
	 * Just set values to garbage.
	 */
	private TrainerProperties() {
		trainerName = null;
	}

	public AiBattleOpponent createInstance(BattleModel model) {
		AiBattleOpponent op = new AiBattleOpponent(trainerName, model);
		return op;
	}
}
