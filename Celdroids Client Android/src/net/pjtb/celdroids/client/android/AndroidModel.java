package net.pjtb.celdroids.client.android;

import net.pjtb.celdroids.client.SceneFactory;
import net.pjtb.celdroids.client.Model;
import net.pjtb.celdroids.client.android.mainmenu.wifidirect.WifiDirectModel;
import net.pjtb.celdroids.client.android.menu.AndroidMainMenuScene;
import net.pjtb.celdroids.client.menu.MainMenuScene;

import com.badlogic.gdx.backends.android.AndroidApplication;

public class AndroidModel extends Model {
	private final AndroidApplication app;

	public WifiDirectModel wifiDirect;

	public AndroidModel(AndroidApplication app) {
		this.app = app;
	}

	@Override
	protected SceneFactory createSceneFactory() {
		return new SceneFactory() {
			@Override
			public MainMenuScene makeMainMenuScene(Model model) {
				return new AndroidMainMenuScene((AndroidModel) model);
			}
		};
	}

	public AndroidApplication getApplication() {
		return app;
	}
}
