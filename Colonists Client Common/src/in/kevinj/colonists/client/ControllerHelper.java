package in.kevinj.colonists.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

public class ControllerHelper {
	private Camera defaultCam;

	public boolean wasBackPressed, wasMenuPressed;

	public ControllerHelper(Camera cam) {
		this.defaultCam = cam;
	}

	public void update(float tDelta) {
		wasBackPressed = Gdx.input.isKeyPressed(Keys.ESCAPE) || Gdx.input.isKeyPressed(Keys.BACK);
		wasMenuPressed = Gdx.input.isKeyPressed(Keys.ENTER) || Gdx.input.isKeyPressed(Keys.MENU);
	}

	public int getCursorX(Camera overrideCam) {
		Vector3 pos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
		if (overrideCam != null)
			overrideCam.unproject(pos);
		else
			defaultCam.unproject(pos);
		return (int) pos.x;
	}

	public int getCursorY(Camera overrideCam) {
		Vector3 pos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
		if (overrideCam != null)
			overrideCam.unproject(pos);
		else
			defaultCam.unproject(pos);
		return (int) pos.y;
	}
}
