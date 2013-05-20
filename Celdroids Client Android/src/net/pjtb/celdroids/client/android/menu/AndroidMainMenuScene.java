package net.pjtb.celdroids.client.android.menu;

import net.pjtb.celdroids.client.Button;
import net.pjtb.celdroids.client.android.AndroidModel;
import net.pjtb.celdroids.client.android.mainmenu.wifidirect.WifiDirectModel;
import net.pjtb.celdroids.client.android.mainmenu.wifidirect.WifiDirectSelectionScene;
import net.pjtb.celdroids.client.menu.MainMenuScene;

public class AndroidMainMenuScene extends MainMenuScene {
	public AndroidMainMenuScene(AndroidModel model) {
		super(model);

		model.wifiDirect = new WifiDirectModel(model);
		subScenes.put(MainMenuSubSceneType.P2P_CONNECT, new WifiDirectSelectionScene(model.wifiDirect, this));

		buttons.add(new Button(model, "Wifi Direct", new Runnable() {
			@Override
			public void run() {
				subScene = subScenes.get(MainMenuSubSceneType.P2P_CONNECT);
				subScene.swappedIn(false);
			}
		}, 300, 10, 256, 128));
	}
}
