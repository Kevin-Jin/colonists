package net.pjtb.celdroids.client.android;

import java.sql.SQLException;

import net.pjtb.celdroids.client.DatabaseManager;
import net.pjtb.celdroids.client.Model;
import net.pjtb.celdroids.client.SceneFactory;
import net.pjtb.celdroids.client.android.menu.AndroidMainMenuScene;
import net.pjtb.celdroids.client.android.menu.wifidirect.WifiDirectModel;
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

	@Override
	public void startLoadingResources(float minSplashTime) {
		try {
			db = new DatabaseManager("jdbc:sqldroid:" + app.getFilesDir().getAbsolutePath() + "/saves.sqlite", null, null, false);
			db.initialize("org.sqldroid.SQLDroidDriver");
			db.cleanup(null, null, db.getConnection()); // test for connection errors
		} catch (SQLException e) {
			e.printStackTrace();
		}
		super.startLoadingResources(minSplashTime);
	}

	public AndroidApplication getApplication() {
		return app;
	}
}
