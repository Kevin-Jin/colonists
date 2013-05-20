package net.pjtb.celdroids.client.android.mainmenu.wifidirect;

import java.net.InetSocketAddress;

import net.pjtb.celdroids.Constants;
import net.pjtb.celdroids.NioSession;
import net.pjtb.celdroids.client.android.AndroidModel;

import com.badlogic.gdx.backends.android.AndroidApplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;

public class WifiDirectModel {
	public final AndroidModel parent;

	private WifiP2pManager wifiDirect;
	private WifiP2pManager.Channel channel;
	private IntentFilter wifiDirectFilter;
	private BroadcastReceiver wifiDirectReceiver;

	public WifiDirectModel(AndroidModel model) {
		this.parent = model;
	}

	public void swappedIn() {
		AndroidApplication app = parent.getApplication();
		wifiDirect = (WifiP2pManager) app.getSystemService(Context.WIFI_P2P_SERVICE);
		channel = wifiDirect.initialize(app, app.getMainLooper(), null);
		wifiDirectFilter = new IntentFilter();
		wifiDirectFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		wifiDirectFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		wifiDirectFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		wifiDirectFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		wifiDirectReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();

				if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
					int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
					if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
						//Wifi Direct is enabled
					} else {
						//Wi-Fi Direct is not enabled
					}
				} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
					wifiDirect.requestPeers(channel, new WifiP2pManager.PeerListListener() {
						@Override
						public void onPeersAvailable(WifiP2pDeviceList peers) {
							//TODO: let user choose
							WifiP2pDevice device = peers.getDeviceList().iterator().next();

							WifiP2pConfig config = new WifiP2pConfig();
							config.deviceAddress = device.deviceAddress;
							wifiDirect.connect(channel, config, new WifiP2pManager.ActionListener() {
								@Override
								public void onSuccess() {
									//handled in WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION
								}

								@Override
								public void onFailure(int reason) {
									
								}
							});
						}
					});
				} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
					NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

		            if (networkInfo.isConnected()) {
		            	//connected
		                wifiDirect.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
							@Override
							public void onConnectionInfoAvailable(WifiP2pInfo info) {
								if (info.groupFormed && info.isGroupOwner)
									NioSession.createServer(new InetSocketAddress(Constants.PORT), Constants.SOCKET_TIMEOUT);
								else
									NioSession.createClient(new InetSocketAddress(info.groupOwnerAddress.getHostAddress(), Constants.PORT), Constants.SOCKET_TIMEOUT);
							}
						});
					} else {
						//disconnected
					}
				} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
					//Respond to this device's wifi state changing
				}
			}
		};
		wifiDirect.discoverPeers(channel, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				//handled in WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION
			}

			@Override
			public void onFailure(int reasonCode) {
				
			}
		});
	}

	public void disconnect() {
		wifiDirect.removeGroup(channel, new ActionListener() {
			@Override
			public void onSuccess() {
				//handled in WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION
			}

			@Override
			public void onFailure(int reasonCode) {
				
			}
		});
	}

	public void pause() {
		parent.getApplication().registerReceiver(wifiDirectReceiver, wifiDirectFilter);
	}

	public void resume() {
		parent.getApplication().unregisterReceiver(wifiDirectReceiver);
	}
}
