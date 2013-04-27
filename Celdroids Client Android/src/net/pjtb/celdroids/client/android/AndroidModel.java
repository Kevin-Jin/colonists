package net.pjtb.celdroids.client.android;

import net.pjtb.celdroids.client.SceneFactory;
import net.pjtb.celdroids.client.Model;
import net.pjtb.celdroids.client.android.scenes.AndroidMainMenuScene;
import net.pjtb.celdroids.client.scenes.MainMenuScene;

import com.badlogic.gdx.backends.android.AndroidApplication;

public class AndroidModel extends Model {
	private final AndroidApplication app;

	public AndroidModel(AndroidApplication app) {
		this.app = app;
	}

	@Override
	protected SceneFactory createSceneFactory() {
		return new SceneFactory() {
			@Override
			public MainMenuScene makeMainMenuScene() {
				return new AndroidMainMenuScene(AndroidModel.this);
			}
		};
	}

	public AndroidApplication getApplication() {
		return app;
	}

	public void resume() {
		getScene().resume();
	}

	public void pause() {
		getScene().pause();
	}
}
