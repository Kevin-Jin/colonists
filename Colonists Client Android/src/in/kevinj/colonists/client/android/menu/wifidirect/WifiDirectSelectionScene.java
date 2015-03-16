package in.kevinj.colonists.client.android.menu.wifidirect;

import in.kevinj.colonists.Constants;
import in.kevinj.colonists.NetworkPlayer;
import in.kevinj.colonists.client.Model;
import in.kevinj.colonists.client.PriorityQueueAssetManager.LoadEntry;
import in.kevinj.colonists.client.Scene;

import java.util.Collection;
import java.util.Collections;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class WifiDirectSelectionScene implements Scene {
	private final WifiDirectModel model;

	private final Scene parentScene;

	private final ShapeRenderer shapeRenderer;

	public WifiDirectSelectionScene(WifiDirectModel model, Scene mainMenuScene) {
		this.model = model;

		this.parentScene = mainMenuScene;

		shapeRenderer = new ShapeRenderer();
	}

	@Override
	public Collection<LoadEntry> getAssetDependencies() {
		return Collections.emptyList();
	}

	@Override
	public Collection<String> getSpriteSheetDependencies() {
		return Collections.emptyList();
	}

	@Override
	public void swappedIn(boolean transition) {
		model.swappedIn();
	}

	@Override
	public void resize(int width, int height) {
		
	}

	@Override
	public void pause() {
		model.pause();
	}

	@Override
	public void resume() {
		model.resume();
	}

	@Override
	public void update(float tDelta) {
		NetworkPlayer op = model.update(tDelta);
		if (op != null) {
			op.onFlee = new Runnable() {
				@Override
				public void run() {
					model.disconnect();
				}
			};

			swappedOut(true);
			model.parent.sceneToShow.setSubscene(null);
			model.parent.worldModel.initRemote(op, !model.isHost);
			model.parent.swapScene(model.parent.scenes.get(Model.SceneType.WORLD));
			return;
		}

		model.selections.update(tDelta);

		if (model.parent.controller.wasBackPressed && !Gdx.input.isKeyPressed(Keys.ESCAPE) && !Gdx.input.isKeyPressed(Keys.BACK)) {
			swappedOut(true);
			parentScene.setSubscene(null);
		} else if (model.parent.controller.wasMenuPressed && !Gdx.input.isKeyPressed(Keys.ENTER) && !Gdx.input.isKeyPressed(Keys.MENU)) {
			
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		batch.end();
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		shapeRenderer.setProjectionMatrix(model.parent.getCamera().combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(0, 0, 0, 0.5f);
		shapeRenderer.rect(0, Constants.HEIGHT, Constants.WIDTH, -Constants.HEIGHT);
		shapeRenderer.end();
		Gdx.gl20.glDisable(GL20.GL_BLEND);
		batch.setProjectionMatrix(model.parent.getCamera().combined);
		batch.begin();

		model.selections.draw(batch);
	}

	@Override
	public void swappedOut(boolean transition) {
		if (transition)
			model.swappedOut();
	}

	@Override
	public Scene getSubscene() {
		return null;
	}

	@Override
	public void setSubscene(Scene scene) {
		
	}
}
