package in.kevinj.colonists.client.menu.lobby;

import in.kevinj.colonists.Constants;
import in.kevinj.colonists.client.Button;
import in.kevinj.colonists.client.Model;
import in.kevinj.colonists.client.NetworkPlayerBattleOpponent;
import in.kevinj.colonists.client.Scene;

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

	private final Button close;

	public AwaitingClientScene(Model m, Scene mainMenuScene) {
		this.model = new AwaitingClientModel(m);

		this.parentScene = mainMenuScene;

		successTint = NumberUtils.intToFloatColor(0xFF << 24 | 0x00 << 16 | 0xFF << 8 | 0xFF);
		errorTint = NumberUtils.intToFloatColor(0xFF << 24 | 0x00 << 16 | 0x00 << 8 | 0xFF);
		shapeRenderer = new ShapeRenderer();

		this.close = new Button(m, "Okay", new Runnable() {
			@Override
			public void run() {
				swappedOut(true);
				parentScene.setSubscene(null);
			}
		}, (Constants.WIDTH - 256) / 2, Constants.HEIGHT - 192, 256, 128);
	}

	@Override
	public void update(float tDelta) {
		NetworkPlayerBattleOpponent op = model.update(tDelta);
		if (op != null) {
			swappedOut(true);
			model.parent.scene.setSubscene(null);
			model.parent.scene.swappedOut(true);
			model.parent.scene = model.parent.scenes.get(Model.SceneType.BATTLE);
			model.parent.battleModel.initRemote(op, false);
			model.parent.scene.swappedIn(true);
			return;
		}

		close.hidden = !model.error;
		close.update(tDelta);

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
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(0, 0, 0, 0.5f);
		shapeRenderer.rect(0, Constants.HEIGHT, Constants.WIDTH, -Constants.HEIGHT);
		shapeRenderer.end();
		Gdx.gl10.glDisable(GL10.GL_BLEND);
		batch.begin();

		Sprite s = model.parent.sprites.get("ui/popup/confirmBackground");
		s.setBounds((Constants.WIDTH - 970) / 2, Constants.HEIGHT - 300, 970, 300);
		s.draw(batch);

		if (model.error)
			close.draw(batch);

		BitmapFont fnt = model.parent.assets.get("fonts/buttons.fnt", BitmapFont.class);
		if (model.message != null) {
			TextBounds bnds = fnt.getBounds(model.message);
			fnt.setColor(model.error ? errorTint : successTint);
			fnt.draw(batch, model.message, (Constants.WIDTH - bnds.width) / 2, Constants.HEIGHT - 300 / 2 - bnds.height);
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
