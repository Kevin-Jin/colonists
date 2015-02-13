package in.kevinj.colonists.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector3;

public class ControllerHelper {
	private ScaleDisplay defaultTransformer;

	public boolean wasBackPressed, wasMenuPressed;

	public ControllerHelper(ScaleDisplay transformer) {
		this.defaultTransformer = transformer;
	}

	public void update(float tDelta) {
		wasBackPressed = Gdx.input.isKeyPressed(Keys.ESCAPE) || Gdx.input.isKeyPressed(Keys.BACK);
		wasMenuPressed = Gdx.input.isKeyPressed(Keys.ENTER) || Gdx.input.isKeyPressed(Keys.MENU);
	}

	public Vector3 getCursor(ScaleDisplay transformer) {
		Vector3 pos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
		if (transformer == null)
			transformer = defaultTransformer;
		transformer.getCamera().unproject(pos, transformer.getViewportX(), transformer.getViewportY(), transformer.getViewportWidth(), transformer.getViewportHeight());
		return pos;
	}
}
