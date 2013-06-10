package net.pjtb.celdroids.client.menu.lobby;

import net.pjtb.celdroids.Constants;
import net.pjtb.celdroids.NioSession;
import net.pjtb.celdroids.client.Model;
import net.pjtb.celdroids.client.Scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.NumberUtils;

public class AwaitingClientScene implements Scene {
	private final AwaitingClientModel model;

	private final Scene parentScene;

	private final float successTint, errorTint;
	private final ShapeRenderer shapeRenderer;

	public AwaitingClientScene(Model m, Scene mainMenuScene) {
		this.model = new AwaitingClientModel(m);

		this.parentScene = mainMenuScene;

		successTint = NumberUtils.intToFloatColor(0xFF << 24 | 0x00 << 16 | 0xFF << 8 | 0xFF);
		errorTint = NumberUtils.intToFloatColor(0xFF << 24 | 0x00 << 16 | 0x00 << 8 | 0xFF);
		shapeRenderer = new ShapeRenderer();
	}

	@Override
	public void update(float tDelta) {
		NioSession ses = model.update(tDelta);
		if (ses != null) {
			swappedOut(true);
			model.parent.scene.setSubscene(null);
			model.parent.scene.swappedOut(true);
			model.parent.scene = model.parent.scenes.get(Model.SceneType.BATTLE);
			model.parent.scene.swappedIn(true);
			return;
		}

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
		shapeRenderer.filledRect(0, 0, Constants.WIDTH, Constants.HEIGHT);
		shapeRenderer.end();
		Gdx.gl10.glDisable(GL10.GL_BLEND);
		batch.begin();

		Sprite s = model.parent.sprites.get("ui/popup/confirmBackground");
		s.setBounds((Constants.WIDTH - 970) / 2, (Constants.HEIGHT - 300), 970, 300);
		s.draw(batch);

		BitmapFont fnt = model.parent.assets.get("fonts/buttons.fnt", BitmapFont.class);
		if (model.message != null) {
			TextBounds bnds = fnt.getBounds(model.message);
			fnt.setColor(model.error ? errorTint : successTint);
			fnt.draw(batch, model.message, (float) ((Constants.WIDTH - bnds.width) / 2), (float) (Constants.HEIGHT - 300 / 2 - bnds.height));
		}
	}

	@Override
	public void swappedIn(boolean transition) {
		if (transition)
			model.swappedIn();
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {
		
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
