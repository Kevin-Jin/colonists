package net.pjtb.celdroids.client;

import net.pjtb.celdroids.Constants;
import net.pjtb.celdroids.NioSession;
import net.pjtb.celdroids.NioSession.IncompleteNioSession;
import net.pjtb.celdroids.Session;

public abstract class ConnectStatusPopupModel {
	public final Model parent;

	public boolean error;
	public String message;

	protected IncompleteNioSession state;
	private NioSession session;

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

	public PlayerBattleOpponent update(float tDelta) {
		if (session != null) {
			Session.PacketReader reader = session.read();
			if (reader == null)
				return null;

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
			String name = reader.getLengthPrefixedAsciiString();
			PlayerBattleOpponent op = new PlayerBattleOpponent(name, session);
			for (byte i = reader.getByte(); i > 0; --i)
				op.party.add(new CeldroidMonster(parent.assets.<CeldroidProperties>get(reader.getLengthPrefixedAsciiString()), null));
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
