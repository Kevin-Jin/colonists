package in.kevinj.colonists.client;

import in.kevinj.colonists.Constants;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

public class ControllerHelper {
	public boolean wasBackPressed, wasMenuPressed;

	public void update(float tDelta) {
		wasBackPressed = Gdx.input.isKeyPressed(Keys.ESCAPE) || Gdx.input.isKeyPressed(Keys.BACK);
		wasMenuPressed = Gdx.input.isKeyPressed(Keys.ENTER) || Gdx.input.isKeyPressed(Keys.MENU);
	}

	public static int getCursorX() {
		return Gdx.input.getX();
	}

	public static int getCursorY() {
		return Constants.HEIGHT - Gdx.input.getY();
	}
}
