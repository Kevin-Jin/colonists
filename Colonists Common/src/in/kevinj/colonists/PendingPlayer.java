package in.kevinj.colonists;

import in.kevinj.colonists.world.Coordinate;
import in.kevinj.colonists.world.PlayerAction;

import java.util.Set;

public class PendingPlayer extends Player {
	public PendingPlayer(String name, Set<Coordinate.NegativeSpace> availableMoves) {
		super(name, availableMoves);
	}

	@Override
	public PlayerAction getNextMove() {
		return null;
	}

	@Override
	public void sendMove(PlayerAction move) {
		if (move instanceof PlayerAction.BeginConsiderMove) {
			//TODO: batch up sequence of other Consider/Commit moves into the net change
		} else if (move instanceof PlayerAction.EndConsiderMove) {
			//TODO: batch up sequence of other Consider/Commit moves into the net change
		} else if (move instanceof PlayerAction.CommitMove) {
			//TODO: batch up sequence of other Consider/Commit moves into the net change
		} else {
			queue.add(move);
		}
	}

	public void transferTo(Player p) {
		p.availableMoves.addAll(this.availableMoves);
		for (PlayerAction move : this.queue)
			p.sendMove(move);
	}
}
