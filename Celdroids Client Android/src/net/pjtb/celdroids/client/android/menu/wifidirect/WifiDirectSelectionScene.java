package net.pjtb.celdroids.client.android.menu.wifidirect;

import net.pjtb.celdroids.Constants;
import net.pjtb.celdroids.client.Model;
import net.pjtb.celdroids.client.NetworkPlayerBattleOpponent;
import net.pjtb.celdroids.client.Scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;
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
	public void swappedIn(boolean transition) {
		model.swappedIn();
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
		NetworkPlayerBattleOpponent op = model.update(tDelta);
		if (op != null) {
			swappedOut(true);
			model.parent.scene.setSubscene(null);
			model.parent.scene.swappedOut(true);
			model.parent.scene = model.parent.scenes.get(Model.SceneType.BATTLE);
			model.parent.battleModel.initRemote(op, !model.isHost);
			model.parent.scene.swappedIn(true);
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
		Gdx.gl10.glEnable(GL10.GL_BLEND);
		Gdx.gl10.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		shapeRenderer.begin(ShapeRenderer.ShapeType.FilledRectangle);
		shapeRenderer.setColor(0, 0, 0, 0.5f);
		shapeRenderer.filledRect(0, Constants.HEIGHT, Constants.WIDTH, -Constants.HEIGHT);
		shapeRenderer.end();
		Gdx.gl10.glDisable(GL10.GL_BLEND);
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
