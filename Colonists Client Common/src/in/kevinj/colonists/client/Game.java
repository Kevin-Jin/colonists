package in.kevinj.colonists.client;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Game implements ApplicationListener {
	private Model model;
	private SpriteBatch batch;
	private boolean paused;

	public Game(Model model) {
		this.model = model;
	}

	@Override
	public void create() {
		// reverse what OpenGL considers front-facing polygons (correction for upside down textures) and enable backface culling
		Gdx.gl10.glFrontFace(GL10.GL_CW);
		Gdx.gl10.glEnable(GL10.GL_CULL_FACE);
		Gdx.gl10.glCullFace(GL10.GL_BACK);
		// enable alpha blending
		Gdx.gl10.glEnable(GL10.GL_BLEND);
		Gdx.gl10.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		Gdx.input.setCatchBackKey(true);
		Gdx.input.setCatchMenuKey(true);

		model.onStart();
		model.startLoadingResources(true);
		batch = new SpriteBatch();
	}

	//FIXME: not called immediately if non-continuous rendering is enabled. i.e.
	//Graphics.requestRendering() not called on resize by libgdx.
	@Override
	public void resize(int width, int height) {
		model.resize(width, height);
	}

	@Override
	public void render() {
		float tDelta = Gdx.graphics.getDeltaTime();
		model.continueLoadingResources(tDelta);

		model.sceneToShow.update(tDelta);
		model.controller.update(tDelta);
		if (paused && Gdx.app.getType() == Application.ApplicationType.Android)
			return;

		Gdx.gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);
		batch.begin();
		model.cam.apply(Gdx.gl10);
		Gdx.gl.glViewport(model.getViewportX(), model.getViewportY(), model.getViewportWidth(), model.getViewportHeight());
		model.sceneToShow.draw(batch);
		batch.end();
	}

	@Override
	public void pause() {
		model.onPause();
		paused = true;
	}

	@Override
	public void resume() {
		paused = false;
		model.onResume();
	}

	@Override
	public void dispose() {
		model.onDispose();
	}
}
