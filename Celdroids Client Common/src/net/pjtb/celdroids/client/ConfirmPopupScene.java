package net.pjtb.celdroids.client;

import net.pjtb.celdroids.Constants;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.NumberUtils;

public class ConfirmPopupScene implements Scene {
	private final Model model;

	private final float fontTint;
	private final String text;
	private final Model.SceneType nextScene;
	private final Button yes, no;

	private final ShapeRenderer shapeRenderer;

	public ConfirmPopupScene(Model m, String text, Model.SceneType nextScene) {
		this.model = m;

		fontTint = NumberUtils.intToFloatColor(0xFF << 24 | 0x00 << 16 | 0x00 << 8 | 0xFF);
		this.text = text;
		this.nextScene = nextScene;
		this.yes = new Button(model, "Yes", new Runnable() {
			@Override
			public void run() {
				proceed();
			}
		}, (Constants.WIDTH - 522) / 2, Constants.HEIGHT / 2 - 100, 256, 128);
		this.no = new Button(model, "No", new Runnable() {
			@Override
			public void run() {
				back();
			}
		}, (Constants.WIDTH - 522) / 2 + 266, Constants.HEIGHT / 2 - 100, 256, 128);

		shapeRenderer = new ShapeRenderer();
	}

	private void proceed() {
		swappedOut(true);
		model.scene.setSubscene(null);
		model.scene.swappedOut(true);

		if (nextScene != null) {
			model.scene = model.scenes.get(nextScene);
			model.scene.swappedIn(true);
		} else {
			Gdx.app.exit();
		}
	}

	private void back() {
		swappedOut(true);
		model.scene.setSubscene(null);
	}

	@Override
	public void update(float tDelta) {
		yes.update(tDelta);
		no.update(tDelta);

		if (model.controller.wasBackPressed && !Gdx.input.isKeyPressed(Keys.ESCAPE) && !Gdx.input.isKeyPressed(Keys.BACK)) {
			back();
		} else if (model.controller.wasMenuPressed && !Gdx.input.isKeyPressed(Keys.ENTER) && !Gdx.input.isKeyPressed(Keys.MENU)) {
			
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

		Sprite s = model.sprites.get("ui/popup/confirmBackground");
		s.setBounds((Constants.WIDTH - 970) / 2, (Constants.HEIGHT - 300) / 2, 970, 300);
		s.draw(batch);

		BitmapFont fnt = model.assets.get("fonts/buttons.fnt", BitmapFont.class);
		TextBounds bnds = fnt.getBounds(text);
		fnt.setColor(fontTint);
		fnt.draw(batch, text, (float) ((Constants.WIDTH - bnds.width) / 2), (float) (Constants.HEIGHT / 2 + bnds.height * 2));
		yes.draw(batch);
		no.draw(batch);
	}

	@Override
	public void swappedIn(boolean transition) {
		
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void swappedOut(boolean transition) {
		
	}

	@Override
	public Scene getSubscene() {
		return null;
	}

	@Override
	public void setSubscene(Scene scene) {
		
	}
}
