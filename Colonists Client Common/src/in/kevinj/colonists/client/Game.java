package in.kevinj.colonists.client;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
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
		Gdx.gl20.glFrontFace(GL20.GL_CW);
		Gdx.gl20.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl20.glCullFace(GL20.GL_BACK);
		// enable alpha blending
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

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

		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.setProjectionMatrix(model.cam.combined);
		batch.begin();
		Gdx.gl20.glViewport(model.getViewportX(), model.getViewportY(), model.getViewportWidth(), model.getViewportHeight());
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
