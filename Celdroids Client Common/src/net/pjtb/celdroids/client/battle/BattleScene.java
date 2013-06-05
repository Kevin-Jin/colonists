package net.pjtb.celdroids.client.battle;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.pjtb.celdroids.Constants;
import net.pjtb.celdroids.client.Button;
import net.pjtb.celdroids.client.CeldroidBattleMove;
import net.pjtb.celdroids.client.CeldroidMonster;
import net.pjtb.celdroids.client.ConfirmPopupScene;
import net.pjtb.celdroids.client.Model;
import net.pjtb.celdroids.client.Scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.NumberUtils;

public class BattleScene implements Scene {
	private static final float MOVE_ANIMATION_TIME = 1.5f, MOVE_FREEZE_TIME = 0.5f;
	private static final float ANIMATION_FREQUENCY = 7; //move animation, in frames per second

	private enum BattleSubSceneType { CONFIRM_FLEE_POPUP }

	private final BattleModel model;

	protected final Map<BattleSubSceneType, Scene> subScenes;
	private Scene subScene;

	private final FanSelect partySwitcher, attackList;
	private final Button runButton;

	private float fontTint;
	private String text;
	private float remainingTextTime;

	private CeldroidBattleMove currentMove;
	private float moveElapsedTime;
	private float moveEndTime;

	private boolean selfTurn, canAct;

	public BattleScene(Model m) {
		this.model = new BattleModel(m);

		subScenes = new EnumMap<BattleSubSceneType, Scene>(BattleSubSceneType.class);
		subScenes.put(BattleSubSceneType.CONFIRM_FLEE_POPUP, new ConfirmPopupScene(m, "Are you sure you want to flee?", Model.SceneType.WORLD));

		runButton = new Button(m, "Flee", new Runnable() {
			@Override
			public void run() {
				flee();
			}
		}, 1172, 576, 108, 144, "ui/battleScene/run", "ui/battleScene/selectedRun", 255, 255, 255, 255, 255, 0, 0, 63);

		partySwitcher = new FanSelect(model.parent, 1 + 120 / 2, Constants.HEIGHT / 2, -Math.PI / 3, Math.PI / 3, 60, 200, "Party");
		attackList = new FanSelect(model.parent, Constants.WIDTH - 200 - 120 / 2, Constants.HEIGHT / 2, 2 * Math.PI / 3, 4 * Math.PI / 3, 60, 200, "Attack");

		fontTint = NumberUtils.intToFloatColor(0xFF << 24 | 0x00 << 16 | 0x00 << 8 | 0xFF);

		selfTurn = true;
		canAct = true;
	}

	private void flee() {
		if (canAct) {
			subScene = subScenes.get(BattleSubSceneType.CONFIRM_FLEE_POPUP);
			subScene.swappedIn(true);
		}
	}

	private void calledMonster() {
		final List<CeldroidBattleMove> moves = model.party.get(0).monsterType.moves;
		String[] moveNames = new String[moves.size()];
		for (int i = 0; i < moveNames.length; i++)
			moveNames[i] = moves.get(i).name;

		attackList.setSelections(new FanSelect.SelectTask() {
			@Override
			public void selected(int index) {
				currentMove = moves.get(index);
				text = model.party.get(0).getName() + " used " + currentMove.name + "!";
				remainingTextTime = 2;
				moveElapsedTime = 0;
				moveEndTime = currentMove.loop ? MOVE_ANIMATION_TIME : (currentMove.frameCount / ANIMATION_FREQUENCY);
				canAct = false;
			}
		}, moveNames);

		text = "Go, " + model.party.get(0).getName() + "!";
		remainingTextTime = 2;
	}

	@Override
	public void swappedIn(boolean transition) {
		if (transition) {
			model.updateParty();

			final CeldroidMonster[] restOfParty = model.party.subList(1, model.party.size()).toArray(new CeldroidMonster[model.party.size() - 1]);
			final String[] selectablePartyNames = new String[restOfParty.length];
			for (int i = 0; i < restOfParty.length; i++)
				selectablePartyNames[i] = restOfParty[i].getName();
			final FanSelect.SelectTask[] partySwitchTask = new FanSelect.SelectTask[1];
			partySwitchTask[0] = new FanSelect.SelectTask() {
				@Override
				public void selected(int index) {
					model.swapPartyLead(index + 1);
					restOfParty[index] = model.party.get(index + 1);
					for (int i = 0; i < restOfParty.length; i++)
						selectablePartyNames[i] = restOfParty[i].getName();
					partySwitcher.setSelections(partySwitchTask[0], selectablePartyNames);
					calledMonster();

					canAct = false;
					selfTurn = false;
				}
			};
			partySwitcher.setSelections(partySwitchTask[0], selectablePartyNames);
			calledMonster();
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

	private void checkForOpponentMove() {
		//TODO: check for network or AI response
		currentMove = model.parent.assets.get("moves/rock.json", CeldroidBattleMove.class);
		text = "Enemy " + "Rock2" + " used " + currentMove.name + "!";
		remainingTextTime = 2;
		moveElapsedTime = 0;
		moveEndTime = currentMove.loop ? MOVE_ANIMATION_TIME : (currentMove.frameCount / ANIMATION_FREQUENCY);
	}

	@Override
	public void update(float tDelta) {
		runButton.hidden = (subScene != null);
		runButton.update(tDelta);
		partySwitcher.hidden = (subScene != null);
		partySwitcher.update(tDelta);
		attackList.hidden = (subScene != null);
		attackList.update(tDelta);
		if (remainingTextTime > 0) {
			remainingTextTime -= tDelta;
			if (remainingTextTime <= 0) {
				remainingTextTime = 0;
				text = null;
			}
		}
		if (currentMove == null && !selfTurn)
			checkForOpponentMove();
		if (currentMove != null) {
			moveElapsedTime += tDelta;
			if (moveElapsedTime > moveEndTime) {
				currentMove = null;
				if (selfTurn) {
					canAct = false;
					selfTurn = false;
				} else {
					canAct = true;
					selfTurn = true;
				}
			}
		}

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
		s = model.parent.sprites.get(model.party.get(0).monsterType.sprite);
		s.setBounds(500, (Constants.HEIGHT - 120) / 2, 120, 120);
		if (!s.isFlipX())
			s.flip(true, false);
		s.draw(batch);

		s = model.parent.sprites.get("character/human2/left/0");
		s.setBounds(Constants.WIDTH - 10 - 120, (Constants.HEIGHT - 120) / 2, 120, 120);
		if (s.isFlipX())
			s.flip(true, false);
		s.draw(batch);
		s = model.parent.sprites.get("monster/rock/evol2");
		s.setBounds(Constants.WIDTH - 200 - 120, (Constants.HEIGHT - 120) / 2, 120, 120);
		if (s.isFlipX())
			s.flip(true, false);
		s.draw(batch);

		if (currentMove != null) {
			s = model.parent.sprites.get(currentMove.spriteDirectory + (int) (moveElapsedTime * ANIMATION_FREQUENCY) % currentMove.frameCount);
			int destX, destY;
			if (currentMove.selfOrigin && moveElapsedTime < (moveEndTime - MOVE_FREEZE_TIME)) {
				int originX;
				int originY;
				if (selfTurn) {
					originX = 500;
					originY = (Constants.HEIGHT - 120) / 2;
					destX = Constants.WIDTH - 200 - 120;
					destY = (Constants.HEIGHT - 120) / 2;
				} else {
					originX = Constants.WIDTH - 200 - 120;
					originY = (Constants.HEIGHT - 120) / 2;
					destX = 500;
					destY = (Constants.HEIGHT - 120) / 2;
				}
				double xVel = (destX - originX) / (moveEndTime - MOVE_FREEZE_TIME);
				double yVel = (destY - originY) / (moveEndTime - MOVE_FREEZE_TIME);
				s.setX((float) (originX + xVel * moveElapsedTime));
				s.setY((float) (originY + yVel * moveElapsedTime));
			} else {
				if (selfTurn) {
					destX = Constants.WIDTH - 200 - 120;
					destY = (Constants.HEIGHT - 120) / 2;
				} else {
					destX = 500;
					destY = (Constants.HEIGHT - 120) / 2;
				}
				s.setX(destX);
				s.setY(destY);
			}
			s.draw(batch);
		}

		if (text != null) {
			BitmapFont fnt = model.parent.assets.get("fonts/buttons.fnt", BitmapFont.class);
			TextBounds bnds = fnt.getBounds(text);
			fnt.setColor(fontTint);
			fnt.draw(batch, text, (float) ((Constants.WIDTH - bnds.width) / 2), (float) (50 + bnds.height));
		}

		if (canAct) {
			runButton.draw(batch);
			partySwitcher.draw(batch);
			attackList.draw(batch);
		}

		if (subScene != null)
			subScene.draw(batch);
	}

	@Override
	public void swappedOut(boolean transition) {
		
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
