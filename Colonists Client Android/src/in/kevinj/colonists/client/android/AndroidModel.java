package in.kevinj.colonists.client.android;

import java.sql.SQLException;

import in.kevinj.colonists.client.DatabaseManager;
import in.kevinj.colonists.client.Model;
import in.kevinj.colonists.client.SceneFactory;
import in.kevinj.colonists.client.android.menu.AndroidMainMenuScene;
import in.kevinj.colonists.client.android.menu.wifidirect.WifiDirectModel;
import in.kevinj.colonists.client.menu.MainMenuScene;

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
	public void startLoadingResources(boolean initialLoad) {
		try {
			db = new DatabaseManager("jdbc:sqldroid:" + app.getFilesDir().getAbsolutePath() + "/saves.sqlite", null, null, false);
			db.initialize("org.sqldroid.SQLDroidDriver");
			db.cleanup(null, null, db.getConnection()); // test for connection errors
		} catch (SQLException e) {
			e.printStackTrace();
		}
		super.startLoadingResources(initialLoad);
	}

	public AndroidApplication getApplication() {
		return app;
	}
}
