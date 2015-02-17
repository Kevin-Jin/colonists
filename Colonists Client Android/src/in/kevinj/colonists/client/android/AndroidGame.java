package in.kevinj.colonists.client.android;

import android.os.Bundle;
import in.kevinj.colonists.client.Game;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidGame extends AndroidApplication {
	private AndroidModel model;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		model = new AndroidModel(this);
		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
		cfg.stencil = 8;
		initialize(new Game(model), cfg);
	}
}
