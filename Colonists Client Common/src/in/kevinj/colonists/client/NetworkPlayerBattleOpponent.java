package in.kevinj.colonists.client;

import in.kevinj.colonists.Session;
import in.kevinj.colonists.client.world.BattleAnimation;
import in.kevinj.colonists.client.world.BattleModel;

public class NetworkPlayerBattleOpponent extends BattleOpponent {
	private final Session ses;

	public Runnable onFlee;

	public NetworkPlayerBattleOpponent(String name, Session ses, BattleModel model) {
		super(name, model);
		this.ses = ses;
	}

	/*@Override
	public BattleAnimation getNextMove(BattleModel model) {
		BattleAnimation anim = super.getNextMove(model);
		if (anim != null)
			return anim;

		Session.PacketReader reader = ses.read();
		if (reader == null)
			return null;

		try {
			switch (reader.getByte()) {
				case OP_ATTACK:
			}
			return null;
		} finally {
			reader.close();
		}
	}*/

	@Override
	public void sendMove(BattleAnimation anim) {
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
