package in.kevinj.colonists.client;

import in.kevinj.colonists.client.world.Coordinate;
import in.kevinj.colonists.client.world.PlayerAction;
import in.kevinj.colonists.client.world.WorldModel;

import java.util.HashSet;

public class AiPlayer extends Player {
	public AiPlayer(String name, WorldModel model) {
		super(name, model, new HashSet<Coordinate.NegativeSpace>());
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
