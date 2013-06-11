package net.pjtb.celdroids.client.menu.lobby;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import net.pjtb.celdroids.Constants;
import net.pjtb.celdroids.NioSession;
import net.pjtb.celdroids.client.ConnectStatusPopupModel;
import net.pjtb.celdroids.client.Model;

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
