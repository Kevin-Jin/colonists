package net.pjtb.celdroids.client.android;

import android.os.Bundle;

import net.pjtb.celdroids.client.Game;

import com.badlogic.gdx.backends.android.AndroidApplication;

public class AndroidGame extends AndroidApplication {
	private AndroidModel model;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		model = new AndroidModel(this);
		initialize(new Game(model), false);
	}
}
