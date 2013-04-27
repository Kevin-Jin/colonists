package net.pjtb.celdroids.client.android;

import net.pjtb.celdroids.client.Game;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;

public class AndroidGame extends AndroidApplication {
	private AndroidModel model;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		model = new AndroidModel(this);
		model.createScenes();
		initialize(new Game(model), false);
		new Thread(new Runnable() {
			@Override
			public void run() {
				model.loadResources();
			}
		}, "resource-loader").start();
	}

	@Override
	protected void onResume() {
		super.onResume();
		model.resume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		model.pause();
	}
}
