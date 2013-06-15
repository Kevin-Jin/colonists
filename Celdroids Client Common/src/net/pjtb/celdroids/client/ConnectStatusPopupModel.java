package net.pjtb.celdroids.client;

import net.pjtb.celdroids.Constants;
import net.pjtb.celdroids.NioSession;
import net.pjtb.celdroids.Session;

public abstract class ConnectStatusPopupModel {
	public final Model parent;

	public boolean error;
	public String message;

	protected NioSession.IncompleteNioSession state;
	protected Session session;

	protected ConnectStatusPopupModel(Model model) {
		this.parent = model;
	}

	protected void progress(String message) {
		this.message = message;
		error = false;
	}

	protected void failed(String message) {
		this.message = message;
		error = true;
	}

	public NetworkPlayerBattleOpponent update(float tDelta) {
		if (session != null) {
			Session.PacketReader reader = session.read();
			if (reader == null)
				return null;

			NetworkPlayerBattleOpponent op;
			try {
				if (reader.getInt() != Constants.FILE_SIGNATURE) {
					session.close();
					failed("Failed service check");
					session = null;
					return null;
				}
				if (reader.getShort() != Constants.VERSION) {
					session.close();
					failed("Peer is on a different version");
					session = null;
					return null;
				}
				op = new NetworkPlayerBattleOpponent(reader.getLengthPrefixedAsciiString(), session, parent.battleModel);
				for (byte i = reader.getByte(); i > 0; --i)
					op.party.add(new CeldroidMonster(parent.assets.get(reader.getLengthPrefixedAsciiString(), CeldroidProperties.class), null));
			} finally {
				reader.close();
			}
			progress("Success!");
			session = null;
			return op;
		}

		if (state == null)
			return null;

		NioSession ses = state.update(tDelta);
		if (state.error != null) {
			failed(state.error);
			state = null;
		} else if (ses != null) {
			progress("Synchronizing battle state...");
			state = null;

			Session.PacketWriter writer = ses.write();
			writer.putInt(Constants.FILE_SIGNATURE);
			writer.putShort(Constants.VERSION);
			writer.putLengthPrefixedAsciiString("<namegoeshere>");
			writer.putByte((byte) 3);
			writer.putLengthPrefixedAsciiString("monsters/fire1.json");
			writer.putLengthPrefixedAsciiString("monsters/water1.json");
			writer.putLengthPrefixedAsciiString("monsters/rock1.json");
			writer.close();

			session = ses;
		}
		return null;
	}

	public void swappedOut() {
		if (state != null)
			state.cancel();
	}
}
