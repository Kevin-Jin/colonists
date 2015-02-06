package in.kevinj.colonists.client.android.menu;

import in.kevinj.colonists.client.Button;
import in.kevinj.colonists.client.android.AndroidModel;
import in.kevinj.colonists.client.android.menu.wifidirect.WifiDirectModel;
import in.kevinj.colonists.client.android.menu.wifidirect.WifiDirectSelectionScene;
import in.kevinj.colonists.client.menu.MainMenuScene;

public class AndroidMainMenuScene extends MainMenuScene {
	public AndroidMainMenuScene(AndroidModel model) {
		super(model);

		model.wifiDirect = new WifiDirectModel(model);
		subScenes.put(MainMenuSubSceneType.P2P_CONNECT, new WifiDirectSelectionScene(model.wifiDirect, this));

		buttons.add(new Button(model, "Wifi Direct", new Runnable() {
			@Override
			public void run() {
				subScene = subScenes.get(MainMenuSubSceneType.P2P_CONNECT);
				subScene.swappedIn(true);
			}
		}, 910, 10, 256, 128));
	}
}
