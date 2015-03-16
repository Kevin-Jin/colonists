package in.kevinj.colonists.client;

import java.util.Set;

import in.kevinj.colonists.client.world.Coordinate;
import in.kevinj.colonists.client.world.PlayerAction;
import in.kevinj.colonists.client.world.WorldModel;

public class LocalPlayer extends Player {
	public LocalPlayer(String name, WorldModel worldModel, Set<Coordinate.NegativeSpace> availableMoves) {
		super(name, worldModel, availableMoves);
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
