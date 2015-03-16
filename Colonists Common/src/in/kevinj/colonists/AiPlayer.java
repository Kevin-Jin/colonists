package in.kevinj.colonists;

import in.kevinj.colonists.world.Coordinate;
import in.kevinj.colonists.world.PlayerAction;

import java.util.HashSet;

public class AiPlayer extends Player {
	public AiPlayer(String name) {
		super(name, new HashSet<Coordinate.NegativeSpace>());
	}

	@Override
	public PlayerAction getNextMove() {
		PlayerAction move = super.getNextMove();
		if (move != null)
			return move;

		// TODO: actual intelligence
		/*List<CeldroidBattleMove> moves = party.get(0).monsterType.moves;
		atkAnimation.reset(model.parent.assets.get(moves.get((int) (Math.random() * moves.size())).file, CeldroidBattleMove.class));
		return atkAnimation;*/
		return null;
	}

	@Override
	public void sendMove(PlayerAction move) {
		
	}
}
