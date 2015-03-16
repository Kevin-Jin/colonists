package in.kevinj.colonists.client;

import in.kevinj.colonists.Constants;
import in.kevinj.colonists.NioSession;
import in.kevinj.colonists.Session;

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

	public NetworkPlayer update(float tDelta) {
		if (session != null) {
			Session.PacketReader reader = session.read();
			if (reader == null)
				return null;

			NetworkPlayer op;
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
				op = new NetworkPlayer(reader.getLengthPrefixedAsciiString(), session, parent.worldModel);
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
