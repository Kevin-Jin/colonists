package net.pjtb.celdroids.client.android.scenes;

import net.pjtb.celdroids.client.android.AndroidModel;
import net.pjtb.celdroids.client.android.WifiDirectModel;
import net.pjtb.celdroids.client.scenes.MainMenuScene;

public class AndroidMainMenuScene extends MainMenuScene {
	public AndroidMainMenuScene(AndroidModel model) {
		model.wifiDirect = new WifiDirectModel(model);
		subScenes.put(MainMenuSubSceneType.P2P_CONNECT, new WifiDirectSelectionScene(model.wifiDirect));
	}
}
