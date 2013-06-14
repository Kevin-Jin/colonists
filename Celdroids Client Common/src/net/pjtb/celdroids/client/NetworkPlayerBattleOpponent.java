package net.pjtb.celdroids.client;

import net.pjtb.celdroids.Session;
import net.pjtb.celdroids.client.battle.AttackAnimation;
import net.pjtb.celdroids.client.battle.BattleAnimation;
import net.pjtb.celdroids.client.battle.BattleModel;
import net.pjtb.celdroids.client.battle.DismissAnimation;

public class NetworkPlayerBattleOpponent extends BattleOpponent {
	private static final byte
		OP_ATTACK = 1,
		OP_SWAP = 2
	;

	private final Session ses;

	public NetworkPlayerBattleOpponent(String name, Session ses, BattleModel model) {
		super(name, model);
		this.ses = ses;

		smnAnimation.reset();
		queue.add(smnAnimation);
	}

	@Override
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
					atkAnimation.reset(model.parent.assets.get(reader.getLengthPrefixedAsciiString(), CeldroidBattleMove.class));
					return atkAnimation;
				case OP_SWAP:
					dmAnimation.reset(reader.getInt());
					smnAnimation.reset();
					queue.add(smnAnimation);
					return dmAnimation;
			}
			return null;
		} finally {
			reader.close();
		}
	}

	@Override
	public void sendMove(BattleAnimation anim) {
		if (anim instanceof AttackAnimation) {
			String move = ((AttackAnimation) anim).move.file;
			Session.PacketWriter writer = ses.write((short) (3 + move.length()));
			try {
				writer.putByte(OP_ATTACK);
				writer.putLengthPrefixedAsciiString(move);
			} finally {
				writer.close();
			}
		} else if (anim instanceof DismissAnimation) {
			Session.PacketWriter writer = ses.write((short) 5);
			try {
				writer.putByte(OP_SWAP);
				writer.putInt(((DismissAnimation) anim).swapWith);
			} finally {
				writer.close();
			}
		}
	}
}
