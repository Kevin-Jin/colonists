package in.kevinj.colonists.client;

import in.kevinj.colonists.client.world.Coordinate;
import in.kevinj.colonists.client.world.PlayerAction;
import in.kevinj.colonists.client.world.WorldModel;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public abstract class Player {
	protected final WorldModel model;
	protected final Queue<PlayerAction> queue;

	public final Set<Coordinate.NegativeSpace> availableMoves;
	public final String name;

	public Player(String name, WorldModel model, Set<Coordinate.NegativeSpace> availableMoves) {
		this.model = model;
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
