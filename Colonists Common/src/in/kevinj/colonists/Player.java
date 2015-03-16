package in.kevinj.colonists;

import in.kevinj.colonists.world.Coordinate;
import in.kevinj.colonists.world.PlayerAction;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public abstract class Player {
	protected final Queue<PlayerAction> queue;

	public final Set<Coordinate.NegativeSpace> availableMoves;
	public final String name;

	public Player(String name, Set<Coordinate.NegativeSpace> availableMoves) {
		this.name = name;
		this.availableMoves = availableMoves;
		queue = new LinkedList<PlayerAction>();
	}

	public PlayerAction getNextMove() {
		if (!queue.isEmpty())
			return queue.poll();
		return null;
	}

	public abstract void sendMove(PlayerAction move);

	public void sendFlee() {
		
	}

	public boolean isPlayable() {
		return false;
	}
}
