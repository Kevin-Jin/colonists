package in.kevinj.colonists.client.world;

import in.kevinj.colonists.Constants;
import in.kevinj.colonists.client.Button;
import in.kevinj.colonists.client.Scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.NumberUtils;

public class BattleScene implements Scene {
	private final BattleModel model;

	private Scene subScene;

	private final FanSelect partySwitcher, attackList;
	private final Button runButton;

	private float fontTint;
	private String text;
	private float remainingTextTime;

	public BattleScene(BattleModel model) {
		this.model = model;

		runButton = new Button(model.parent, "Flee", new Runnable() {
			@Override
			public void run() {
				flee();
			}
		}, 1172, 576, 108, 144, "ui/battleScene/run", "ui/battleScene/selectedRun", 255, 255, 255, 255, 255, 0, 0, 63);

		partySwitcher = new FanSelect(model.parent, 10 + 120 / 2, Constants.HEIGHT / 2, -Math.PI / 3, Math.PI / 3, 60, 200, "Party");
		attackList = new FanSelect(model.parent, 500 + 120 / 2, Constants.HEIGHT / 2, -Math.PI / 3, Math.PI / 3, 60, 200, "Attack");

		fontTint = NumberUtils.intToFloatColor(0xFF << 24 | 0xFF << 16 | 0x00 << 8 | 0x00);
	}

	private void flee() {
		if (model.canAct) {
			subScene = model.subScenes.get(BattleModel.BattleSubSceneType.CONFIRM_FLEE_POPUP);
			subScene.swappedIn(true);
		}
	}

	@Override
	public void swappedIn(boolean transition) {
		Gdx.gl10.glClearColor(0.5f, 1, 0.5f, 1);

		if (transition) {
			model.selfTurn = model.canAct = false;
		}
	}

	@Override
	public void resize(int width, int height) {
		
	}

	@Override
	public void pause() {
		if (subScene != null)
			subScene.pause();
	}

	@Override
	public void resume() {
		if (subScene != null)
			subScene.resume();
	}

	private void checkForOwnMove() {
		/*model.currentAnimation = model.self.getNextMove(model);
		if (model.currentAnimation instanceof AttackAnimation) {
			text = model.self.party.get(0).getName() + " used " + model.self.atkAnimation.move.name + "!";
			remainingTextTime = 2;
			model.canAct = false;
		}*/
	}

	private void checkForOpponentMove() {
		/*model.currentAnimation = model.op.getNextMove(model);
		if (model.currentAnimation instanceof AttackAnimation) {
			text = "Enemy " + model.op.party.get(0).getName() + " used " + model.op.atkAnimation.move.name + "!";
			remainingTextTime = 2;
		}*/
	}

	@Override
	public void update(float tDelta) {
		runButton.hidden = (subScene != null || !model.canAct);
		runButton.update(tDelta);
		partySwitcher.hidden = (subScene != null || !model.canAct);
		partySwitcher.update(tDelta);
		attackList.hidden = (subScene != null || !model.canAct);
		attackList.update(tDelta);
		if (remainingTextTime > 0) {
			remainingTextTime -= tDelta;
			if (remainingTextTime <= 0) {
				remainingTextTime = 0;
				text = null;
			}
		}
		if (model.currentAnimation != null)
			model.currentAnimation.update(tDelta);
		if (model.currentAnimation == null)
			if (model.selfTurn)
				checkForOwnMove();
			else
				checkForOpponentMove();

		if (subScene == null) {
			if (model.parent.controller.wasBackPressed && !Gdx.input.isKeyPressed(Keys.ESCAPE) && !Gdx.input.isKeyPressed(Keys.BACK)) {
				flee();
			} else if (model.parent.controller.wasMenuPressed && !Gdx.input.isKeyPressed(Keys.ENTER) && !Gdx.input.isKeyPressed(Keys.MENU)) {
				
			}
		} else {
			subScene.update(tDelta);
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		if (model.currentAnimation != null)
			model.currentAnimation.draw(batch);

		if (text != null) {
			BitmapFont fnt = model.parent.assets.get("fonts/buttons.fnt", BitmapFont.class);
			TextBounds bnds = fnt.getBounds(text);
			fnt.setColor(fontTint);
			fnt.draw(batch, text, (Constants.WIDTH - bnds.width) / 2, 50 + bnds.height);
		}

		if (model.canAct) {
			runButton.draw(batch);
			partySwitcher.draw(batch);
			attackList.draw(batch);
		}

		if (subScene != null)
			subScene.draw(batch);
	}

	@Override
	public void swappedOut(boolean transition) {
		if (transition)
			model.op.sendFlee();
	}

	@Override
	public Scene getSubscene() {
		return subScene;
	}

	@Override
	public void setSubscene(Scene scene) {
		this.subScene = scene;
	}
}
