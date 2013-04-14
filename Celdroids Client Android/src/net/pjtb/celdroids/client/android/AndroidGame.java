package net.pjtb.celdroids.client.android;

import net.pjtb.celdroids.client.Game;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;

public class AndroidGame extends AndroidApplication {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize(new Game(), false);
	}
}
