package net.pjtb.celdroids.client.battle;

import java.util.List;

import net.pjtb.celdroids.Constants;
import net.pjtb.celdroids.client.Button;
import net.pjtb.celdroids.client.CeldroidBattleMove;
import net.pjtb.celdroids.client.CeldroidMonster;
import net.pjtb.celdroids.client.Scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.Sprite;
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

	private CeldroidMonster[] selfPartyNoLeader;
	private String[] selfPartyNoLeaderNames;
	private FanSelect.SelectTask partySwitchTask;

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

			selfPartyNoLeader = model.self.party.subList(1, model.self.party.size()).toArray(new CeldroidMonster[model.self.party.size() - 1]);
			selfPartyNoLeaderNames = new String[selfPartyNoLeader.length];
			partySwitchTask = new FanSelect.SelectTask() {
				@Override
				public void selected(int index) {
					model.self.dmAnimation.reset(index + 1);
					model.self.sendMove(model.self.dmAnimation);
					model.op.sendMove(model.self.dmAnimation);

					model.self.smnAnimation.reset();
					model.self.sendMove(model.self.smnAnimation);
					model.op.sendMove(model.self.smnAnimation);
				}
			};
			for (int i = 0; i < selfPartyNoLeader.length; i++)
				selfPartyNoLeaderNames[i] = selfPartyNoLeader[i].getName();
			partySwitcher.setSelections(partySwitchTask, selfPartyNoLeaderNames);

			model.showOpponentCeldroid = model.showSelfCeldroid = false;
			model.self.smnAnimation.reset();
			model.self.sendMove(model.self.smnAnimation);
			model.op.sendMove(model.self.smnAnimation);
		}
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
		model.currentAnimation = model.self.getNextMove(model);
		if (model.currentAnimation instanceof AttackAnimation) {
			text = model.self.party.get(0).getName() + " used " + model.self.atkAnimation.move.name + "!";
			remainingTextTime = 2;
			model.canAct = false;
		} else if (model.currentAnimation instanceof SummonAnimation) {
			final List<CeldroidBattleMove> moves = model.self.party.get(0).monsterType.moves;
			String[] moveNames = new String[moves.size()];
			for (int i = 0; i < moveNames.length; i++)
				moveNames[i] = moves.get(i).name;
			attackList.setSelections(new FanSelect.SelectTask() {
				@Override
				public void selected(int index) {
					CeldroidBattleMove move = moves.get(index);
					model.self.atkAnimation.reset(move);
					model.self.sendMove(model.self.atkAnimation);
					model.op.sendMove(model.self.atkAnimation);
				}
			}, moveNames);

			text = "Go, " + model.self.party.get(0).getName() + "!";
			remainingTextTime = 2;
			model.canAct = false;
		} else if (model.currentAnimation instanceof DismissAnimation) {
			int swapWith = ((DismissAnimation) model.currentAnimation).swapWith;
			CeldroidMonster currentLead = model.self.party.get(0);
			model.self.party.set(0, model.self.party.get(swapWith));
			model.self.party.set(swapWith, currentLead);
			selfPartyNoLeader[swapWith - 1] = model.self.party.get(swapWith);
			for (int i = 0; i < selfPartyNoLeader.length; i++)
				selfPartyNoLeaderNames[i] = selfPartyNoLeader[i].getName();
			partySwitcher.setSelections(partySwitchTask, selfPartyNoLeaderNames);

			text = "Come back, " + model.self.party.get(swapWith).getName() + "!";
			remainingTextTime = 2;
			model.canAct = false;
		}
	}

	private void checkForOpponentMove() {
		model.currentAnimation = model.op.getNextMove(model);
		if (model.currentAnimation instanceof AttackAnimation) {
			text = "Enemy " + model.op.party.get(0).getName() + " used " + model.op.atkAnimation.move.name + "!";
			remainingTextTime = 2;
		} else if (model.currentAnimation instanceof SummonAnimation) {
			text = model.op.name + " summoned " + model.op.party.get(0).getName();
			remainingTextTime = 2;
		} else if (model.currentAnimation instanceof DismissAnimation) {
			int swapWith = ((DismissAnimation) model.currentAnimation).swapWith;
			CeldroidMonster currentLead = model.op.party.get(0);
			model.op.party.set(0, model.op.party.get(swapWith));
			model.op.party.set(swapWith, currentLead);

			text = model.op.name + " called back " + model.op.party.get(swapWith).getName();
			remainingTextTime = 2;
		}
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
		Sprite s = model.parent.sprites.get("character/human1/left/0");
		s.setBounds(10, (Constants.HEIGHT - 120) / 2, 120, 120);
		if (!s.isFlipX())
			s.flip(true, false);
		s.draw(batch);
		if (model.showSelfCeldroid) {
			s = model.parent.sprites.get(model.self.party.get(0).monsterType.sprite);
			s.setBounds(500, (Constants.HEIGHT - 120) / 2, 120, 120);
			if (!s.isFlipX())
				s.flip(true, false);
			s.draw(batch);
		}

		s = model.parent.sprites.get("character/human2/left/0");
		s.setBounds(Constants.WIDTH - 10 - 120, (Constants.HEIGHT - 120) / 2, 120, 120);
		if (s.isFlipX())
			s.flip(true, false);
		s.draw(batch);
		if (model.showOpponentCeldroid) {
			s = model.parent.sprites.get(model.op.party.get(0).monsterType.sprite);
			s.setBounds(Constants.WIDTH - 200 - 120, (Constants.HEIGHT - 120) / 2, 120, 120);
			if (s.isFlipX())
				s.flip(true, false);
			s.draw(batch);
		}

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
