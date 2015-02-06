package in.kevinj.colonists.client.android.menu.wifidirect;

import java.net.InetSocketAddress;
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
import android.os.Handler;

import in.kevinj.colonists.Constants;
import in.kevinj.colonists.NioSession;
import in.kevinj.colonists.client.ConnectStatusPopupModel;
import in.kevinj.colonists.client.NetworkPlayerBattleOpponent;
import in.kevinj.colonists.client.android.AndroidModel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;

//TODO: 30 second timeout on "Getting peer permission...", or some more robust way of figuring out when the peer denies or ignores?
public class WifiDirectModel extends ConnectStatusPopupModel {
	public final AndroidModel parent;

	private final Handler mainThreadHandler;

	private WifiP2pManager wifiDirect;
	private WifiP2pManager.Channel channel;
	private IntentFilter wifiDirectFilter;
	private BroadcastReceiver wifiDirectReceiver;
	private boolean shouldScan, scanning;

	public final ScrollableListPane<String> selections;

	public boolean isHost;

	public WifiDirectModel(AndroidModel model) {
		super(model);
		this.parent = model;
		shouldScan = true;

		mainThreadHandler = new Handler(parent.getApplication().getApplicationContext().getMainLooper());
		selections = new ScrollableListPane<String>("Initializing...", model, new ScrollableListPane.SelectTask<String>() {
			@Override
			public void selected(final String deviceAddress) {
				mainThreadHandler.post(new Runnable() {
					@Override
					public void run() {
						WifiP2pConfig config = new WifiP2pConfig();
						config.deviceAddress = deviceAddress;
						wifiDirect.connect(channel, config, new WifiP2pManager.ActionListener() {
							@Override
							public void onSuccess() {
								shouldScan = false;
								progress("Getting peer permission...");
							}

							@Override
							public void onFailure(int reasonCode) {
								failed("Could not connect, reason: " + reasonCode);
							}
						});
					}
				});
			}
		});
	}

	@Override
	protected void progress(String message) {
		selections.text = message;
		selections.error = false;
	}

	@Override
	protected void failed(String message) {
		selections.text = message;
		selections.error = true;
	}

	@Override
	public NetworkPlayerBattleOpponent update(float tDelta) {
		NetworkPlayerBattleOpponent op = super.update(tDelta);
		if (op == null && session == null && state == null && !scanning && shouldScan) {
			mainThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					if (!scanning && shouldScan) {
						scanning = true;
						wifiDirect.discoverPeers(channel, new WifiP2pManager.ActionListener() {
							@Override
							public void onSuccess() {
								// handled in WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION
							}

							@Override
							public void onFailure(int reasonCode) {
								failed("Could not scan, reason: " + reasonCode);
							}
						});
					}
				}
			});
		}
		return op;
	}

	@Override
	public void swappedOut() {
		mainThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				parent.getApplication().unregisterReceiver(wifiDirectReceiver);
				scanning = false;
				shouldScan = false;
			}
		});
	}

	public void swappedIn() {
		mainThreadHandler.post(new Runnable() {
			@Override
			public void run() {
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
							if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
								progress("Scanning...");
							else
								failed("Feature not supported");
						} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
							wifiDirect.requestPeers(channel, new WifiP2pManager.PeerListListener() {
								@Override
								public void onPeersAvailable(WifiP2pDeviceList peers) {
									// TODO: pre-association service discovery
									if (shouldScan) {
										selections.text = null;
										selections.clearSelections();
										for (WifiP2pDevice device : peers.getDeviceList())
											selections.addSelection(device.deviceName, device.deviceAddress);
									}
								}
							});
						} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
							NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

							if (networkInfo.isConnected()) {
								// connected
								wifiDirect.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
									@Override
									public void onConnectionInfoAvailable(final WifiP2pInfo info) {
										Gdx.app.postRunnable(new Runnable() {
											@Override
											public void run() {
												progress("Attempting connection...");
												if (info.groupFormed && info.isGroupOwner) {
													state = NioSession.beginCreateServer(new InetSocketAddress(Constants.PORT), Constants.SOCKET_TIMEOUT);
													isHost = true;
												} else {
													state = NioSession.beginCreateClient(new InetSocketAddress(info.groupOwnerAddress.getHostAddress(), Constants.PORT), Constants.SOCKET_TIMEOUT);
													isHost = false;
												}
											}
										});
									}
								});
							} else {
								// disconnected
							}
						} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
							// Respond to this device's wifi state changing
						}
					}
				};

				shouldScan = true;
				parent.getApplication().registerReceiver(wifiDirectReceiver, wifiDirectFilter);
			}
		});
	}

	public void disconnect() {
		mainThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				wifiDirect.removeGroup(channel, new ActionListener() {
					@Override
					public void onSuccess() {
						// handled in WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION
					}

					@Override
					public void onFailure(int reasonCode) {
						
					}
				});
			}
		});
	}

	public void pause() {
		mainThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				parent.getApplication().unregisterReceiver(wifiDirectReceiver);
				scanning = false;
				shouldScan = false;
			}
		});
	}

	public void resume() {
		mainThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				shouldScan = true;
				parent.getApplication().registerReceiver(wifiDirectReceiver, wifiDirectFilter);
			}
		});
	}
}
