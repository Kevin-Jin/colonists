package net.pjtb.celdroids.client.battle;

import java.util.EnumMap;
import java.util.Map;

import net.pjtb.celdroids.Constants;
import net.pjtb.celdroids.client.Button;
import net.pjtb.celdroids.client.ConfirmPopupScene;
import net.pjtb.celdroids.client.Model;
import net.pjtb.celdroids.client.Scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BattleScene implements Scene {
	private enum BattleSubSceneType { CONFIRM_FLEE_POPUP }

	private final BattleModel model;

	protected final Map<BattleSubSceneType, Scene> subScenes;
	private Scene subScene;

	private final FanSelect partySwitcher, attackList;
	private final Button runButton;

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

		partySwitcher = new FanSelect(model.parent, 100 + 120 / 2, Constants.HEIGHT / 2, -Math.PI / 3, Math.PI / 3, 60, 200, "Party");
		attackList = new FanSelect(model.parent, Constants.WIDTH - 100 - 120 / 2, Constants.HEIGHT / 2, 2 * Math.PI / 3, 4 * Math.PI / 3, 60, 240, "Attack");

		partySwitcher.setSelections(new FanSelect.SelectTask() {
			@Override
			public void selected(int index) {
				switch (index) {
					
				}
			}
		}, "Celdroid 1", "Celdroid 2", "Celdroid 3", "Celdroid 4", "Celdroid 5", "Celdroid 6");
		attackList.setSelections(new FanSelect.SelectTask() {
			@Override
			public void selected(int index) {
				
			}
		}, "Move 1", "Move 2", "Move 3", "Move 4");
	}

	private void flee() {
		subScene = subScenes.get(BattleSubSceneType.CONFIRM_FLEE_POPUP);
		subScene.swappedIn(true);
	}

	@Override
	public void swappedIn(boolean transition) {
		
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

	@Override
	public void update(float tDelta) {
		runButton.hidden = (subScene != null);
		runButton.update(tDelta);
		partySwitcher.hidden = (subScene != null);
		partySwitcher.update(tDelta);
		attackList.hidden = (subScene != null);
		attackList.update(tDelta);

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
		s.setBounds(100, (Constants.HEIGHT - 120) / 2, 120, 120);
		if (!s.isFlipX())
			s.flip(true, false);
		s.draw(batch);

		s = model.parent.sprites.get("character/human1/left/0");
		s.setBounds(Constants.WIDTH - 100 - 120, (Constants.HEIGHT - 120) / 2, 120, 120);
		if (s.isFlipX())
			s.flip(true, false);
		s.draw(batch);

		runButton.draw(batch);
		partySwitcher.draw(batch);
		attackList.draw(batch);

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
