package net.pjtb.celdroids.client.battle;

import net.pjtb.celdroids.Constants;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SummonAnimation extends BattleAnimation {
	private static final float ANIMATION_TIME = 1.5f; //total time to show animation, in seconds
	private static final float FREEZE_TIME = 1f; //time out of total to show final animation state, in seconds

	public boolean swapTurnsAtEnd; //for special case at beginning of battle when we can move after we summon

	public SummonAnimation(BattleModel model) {
		super(model);
		useTurn = true;
	}

	@Override
	public void update(float tDelta) {
		boolean wasOwnTurn = model.selfTurn;
		super.update(tDelta);
		if (moveElapsedTime > moveEndTime) {
			if (wasOwnTurn) {
				model.showSelfCeldroid = true;
				if (!swapTurnsAtEnd) {
					//TODO: assign useTurn to true before super.update, instead of having to do this...
					swapTurnsAtEnd = true;
					model.selfTurn = !model.selfTurn;
					model.canAct = model.selfTurn;
				}
			} else {
				model.showOpponentCeldroid = true;
			}
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		if (model.selfTurn) {
			Sprite s = model.parent.sprites.get(model.self.party.get(0).monsterType.sprite);
			float sideDimension = Math.min(moveElapsedTime / (ANIMATION_TIME - FREEZE_TIME), 1) * 120;
			s.setBounds(500 + (120 - sideDimension) / 2, (Constants.HEIGHT - sideDimension) / 2, sideDimension, sideDimension);
			if (!s.isFlipX())
				s.flip(true, false);
			s.draw(batch);
		} else {
			Sprite s = model.parent.sprites.get(model.op.party.get(0).monsterType.sprite);
			float sideDimension = Math.min(moveElapsedTime / (ANIMATION_TIME - FREEZE_TIME), 1) * 120;
			s.setBounds(Constants.WIDTH - 200 - 120 + (120 - sideDimension) / 2, (Constants.HEIGHT - sideDimension) / 2, sideDimension, sideDimension);
			if (s.isFlipX())
				s.flip(true, false);
			s.draw(batch);
		}
	}

	public void reset() {
		moveElapsedTime = 0;
		moveEndTime = ANIMATION_TIME;
	}
}
