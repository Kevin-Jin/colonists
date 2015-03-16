package in.kevinj.colonists;

import in.kevinj.colonists.world.Coordinate;
import in.kevinj.colonists.world.PlayerAction;

import java.util.Set;

public class LocalPlayer extends Player {
	public LocalPlayer(String name, Set<Coordinate.NegativeSpace> availableMoves) {
		super(name, availableMoves);
	}

	@Override
	public void sendMove(PlayerAction move) {
		queue.add(move);
	}

	@Override
	public boolean isPlayable() {
		return true;
	}
}
