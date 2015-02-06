package in.kevinj.colonists.client.menu.lobby;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import in.kevinj.colonists.Constants;
import in.kevinj.colonists.NioSession;
import in.kevinj.colonists.client.ConnectStatusPopupModel;
import in.kevinj.colonists.client.Model;

public class AwaitingClientModel extends ConnectStatusPopupModel {
	public AwaitingClientModel(Model model) {
		super(model);
	}

	public void swappedIn() {
		assert state == null && !error;

		progress("Awaiting connection...");
		state = NioSession.beginCreateServer(new InetSocketAddress((InetAddress) null, Constants.PORT), 0);
	}
}
