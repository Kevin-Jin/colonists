package in.kevinj.colonists.client;

import in.kevinj.colonists.Constants;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Scaling;

public abstract class ScaleDisplay {
	protected final OrthographicCamera cam;
	private int x, y, width, height;

	protected ScaleDisplay() {
		cam = new OrthographicCamera();
	}

	public void resize(int screenWidth, int screenHeight) {
		if (4 * (screenWidth - 1) > 3 * (screenHeight + 1)) {
			//aspect ratio is less than ~3/4. pillarbox
			Vector2 v = Scaling.fit.apply(Constants.WIDTH, Constants.WIDTH * 4 / 3f, screenWidth, screenHeight);
			width = (int) (v.x + 0.5);
			height = (int) (v.y + 0.5);
			cam.setToOrtho(false, Constants.WIDTH, Constants.WIDTH * 4 / 3f);
		} else if (16 * (screenWidth + 1) < 9 * (screenHeight - 1)) {
			//aspect ratio is more than ~9/16. letterbox
			Vector2 v = Scaling.fit.apply(Constants.WIDTH, Constants.WIDTH * 16 / 9f, screenWidth, screenHeight);
			width = (int) (v.x + 0.5);
			height = (int) (v.y + 0.5);
			cam.setToOrtho(false, Constants.WIDTH, Constants.WIDTH * 16 / 9f);
		} else {
			//aspect ratio is between 2:3 ~ 9:16. crop the top and bottom
			Vector2 v = Scaling.fillX.apply(Constants.WIDTH, Constants.HEIGHT, screenWidth, screenHeight);
			width = (int) (v.x + 0.5);
			height = (int) (v.y + 0.5);
			cam.setToOrtho(false, Constants.WIDTH, Constants.HEIGHT);
		}
		x = (screenWidth - width) / 2;
		y = (screenHeight - height) / 2;
	}

	public Camera getCamera() {
		return cam;
	}

	public int getViewportX() {
		return x;
	}

	public int getViewportY() {
		return y;
	}

	public int getViewportWidth() {
		return width;
	}

	public int getViewportHeight() {
		return height;
	}
}