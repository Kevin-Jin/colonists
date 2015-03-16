package in.kevinj.colonists.client;

import in.kevinj.colonists.Session;
import in.kevinj.colonists.client.world.Coordinate;
import in.kevinj.colonists.client.world.PlayerAction;
import in.kevinj.colonists.client.world.WorldModel;

import java.util.HashSet;

public class NetworkPlayer extends Player {
	private Session ses;

	public Runnable onFlee;

	public NetworkPlayer(String name, Session ses, WorldModel model) {
		super(name, model, new HashSet<Coordinate.NegativeSpace>());
		this.ses = ses;
	}

	@Override
	public PlayerAction getNextMove() {
		PlayerAction move = super.getNextMove();
		if (move != null)
			return move;

		/*Session.PacketReader reader = ses.read();
		if (reader == null)
			return null;

		try {
			switch (reader.getByte()) {
				case OP_ATTACK:
			}
			return null;
		} finally {
			reader.close();
		}*/
		return null;
	}

	@Override
	public void sendMove(PlayerAction move) {
		/*Session.PacketWriter writer = ses.write();
		try {
			writer.putByte(OP_ATTACK);
		} finally {
			writer.close();
		}*/
	}

	@Override
	public void sendFlee() {
		ses.close();
		if (onFlee != null)
			onFlee.run();
	}
}
