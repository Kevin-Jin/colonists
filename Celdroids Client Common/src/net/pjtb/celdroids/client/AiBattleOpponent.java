package net.pjtb.celdroids.client;

import java.util.List;

public class AiBattleOpponent extends BattleOpponent {
	public AiBattleOpponent(String name) {
		super(name);
	}

	@Override
	public String nextMove() {
		//TODO: actual intelligence
		List<CeldroidBattleMove> moves = party.get(0).monsterType.moves;
		return moves.get((int) (Math.random() * moves.size())).file;
	}
}
