package net.pjtb.celdroids.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

public class ControllerHelper {
	public boolean wasBackPressed, wasMenuPressed;

	public void update(float tDelta) {
		wasBackPressed = Gdx.input.isKeyPressed(Keys.ESCAPE) || Gdx.input.isKeyPressed(Keys.BACK);
		wasMenuPressed = Gdx.input.isKeyPressed(Keys.ENTER) || Gdx.input.isKeyPressed(Keys.MENU);
	}
}
