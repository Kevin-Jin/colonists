package net.pjtb.celdroids.client.menu.directconnect;

import net.pjtb.celdroids.Constants;
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

public class DirectConnectSelectionScene implements Scene {
	private final DirectConnectModel model;

	private final Scene parentScene;

	private final float promptTint, entryTint, successTint, errorTint, inactiveTint;
	private final ShapeRenderer shapeRenderer;

	public DirectConnectSelectionScene(Model m, Scene mainMenuScene) {
		this.model = new DirectConnectModel(m);

		this.parentScene = mainMenuScene;

		promptTint = NumberUtils.intToFloatColor(0xFF << 24 | 0xFF << 16 | 0x00 << 8 | 0x00);
		entryTint = NumberUtils.intToFloatColor(0xFF << 24 | 0x00 << 16 | 0x00 << 8 | 0xFF);
		successTint = NumberUtils.intToFloatColor(0xFF << 24 | 0x00 << 16 | 0xFF << 8 | 0xFF);
		errorTint = NumberUtils.intToFloatColor(0xFF << 24 | 0x00 << 16 | 0x00 << 8 | 0xFF);
		inactiveTint = NumberUtils.intToFloatColor(0xFF << 24 | 0xBF << 16 | 0xBF << 8 | 0xBF);
		shapeRenderer = new ShapeRenderer();
	}

	@Override
	public void swappedIn(boolean transition) {
		Gdx.input.setOnscreenKeyboardVisible(true);
		Gdx.input.setInputProcessor(model);
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void update(float tDelta) {
		model.update(tDelta);
		if (model.parent.controller.wasBackPressed && !Gdx.input.isKeyPressed(Keys.ESCAPE) && !Gdx.input.isKeyPressed(Keys.BACK)) {
			swappedOut(true);
			parentScene.setSubscene(null);
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
		TextBounds bnds = fnt.getBounds("Enter the address:");
		fnt.setColor(promptTint);
		fnt.draw(batch, "Enter the address:", (float) ((Constants.WIDTH - bnds.width) / 2), (float) (Constants.HEIGHT - 300 / 2 + bnds.height * 2));
		bnds = fnt.getBounds(model.entered + "_");
		fnt.setColor(model.inactive ? inactiveTint : entryTint);
		fnt.draw(batch, model.entered + "_", (float) ((Constants.WIDTH - bnds.width) / 2), (float) (Constants.HEIGHT + (bnds.height - 300) / 2));
		if (model.message != null) {
			bnds = fnt.getBounds(model.message);
			fnt.setColor(model.error ? errorTint : successTint);
			fnt.draw(batch, model.message, (float) ((Constants.WIDTH - bnds.width) / 2), (float) (Constants.HEIGHT - 300 / 2 - bnds.height));
		}
	}

	@Override
	public void swappedOut(boolean transition) {
		Gdx.input.setInputProcessor(null);
		Gdx.input.setOnscreenKeyboardVisible(false);
	}

	@Override
	public Scene getSubscene() {
		return null;
	}

	@Override
	public void setSubscene(Scene scene) {
		
	}
}
