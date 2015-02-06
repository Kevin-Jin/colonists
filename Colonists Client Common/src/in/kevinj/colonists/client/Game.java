package in.kevinj.colonists.client;

import in.kevinj.colonists.Constants;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Game implements ApplicationListener {
	private Model model;
	private SpriteBatch batch;

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
		// set up viewport
		Gdx.gl10.glViewport(0, 0, Constants.WIDTH, Constants.HEIGHT);

		// switch to projection matrix to set an orthographic projection
		Gdx.gl10.glMatrixMode(GL10.GL_PROJECTION);
		Gdx.gl10.glLoadIdentity();
		// sets origin to bottom left corner of screen - beware that textures still have to be flipped vertically
		Gdx.gl10.glOrthof(0, Constants.WIDTH, 0, Constants.HEIGHT, 0, 100);

		// we never change back to projection matrix, so switch matrix mode here once
		Gdx.gl10.glMatrixMode(GL10.GL_MODELVIEW);

		Gdx.input.setCatchBackKey(true);
		Gdx.input.setCatchMenuKey(true);

		model.startLoadingResources(Constants.SPLASH_SCREEN_MIN_TIME);
		model.onStart();
		batch = new SpriteBatch();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render() {
		float tDelta = Gdx.graphics.getDeltaTime();
		model.continueLoadingResources(tDelta);

		model.scene.update(tDelta);
		model.controller.update(tDelta);
		Gdx.gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);
		batch.begin();
		model.scene.draw(batch);
		batch.end();
	}

	@Override
	public void pause() {
		model.releaseAllResources();
		model.onPause();
	}

	@Override
	public void resume() {
		// TODO: don't release nonvolatile assets in pause() and load only volatile assets (e.g. textures) here
		model.startLoadingResources(0);
		model.onResume();
	}

	@Override
	public void dispose() {
		model.onDispose();
	}
}
