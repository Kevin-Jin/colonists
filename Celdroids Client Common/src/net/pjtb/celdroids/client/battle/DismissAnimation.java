package net.pjtb.celdroids.client.battle;

import net.pjtb.celdroids.Constants;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class DismissAnimation extends BattleAnimation {
	private static final float ANIMATION_TIME = 1.5f; // total time to show animation, in seconds
	private static final float FREEZE_TIME = 1; // time out of total to show final animation state, in seconds

	public int swapWith;

	public DismissAnimation(BattleModel model) {
		super(model);
		useTurn = false;
	}

	@Override
	public void update(float tDelta) {
		super.update(tDelta);
	}

	@Override
	public void draw(SpriteBatch batch) {
		if (model.selfTurn) {
			Sprite s = model.parent.sprites.get(model.self.party.get(swapWith).monsterType.sprite);
			float sideDimension = Math.max(1 - moveElapsedTime / (ANIMATION_TIME - FREEZE_TIME), 0) * 120;
			s.setBounds(500 + (120 - sideDimension) / 2, (Constants.HEIGHT - sideDimension) / 2, sideDimension, sideDimension);
			if (!s.isFlipX())
				s.flip(true, false);
			s.draw(batch);
		} else {
			Sprite s = model.parent.sprites.get(model.op.party.get(swapWith).monsterType.sprite);
			float sideDimension = Math.max(1 - moveElapsedTime / (ANIMATION_TIME - FREEZE_TIME), 0) * 120;
			s.setBounds(Constants.WIDTH - 200 - 120 + (120 - sideDimension) / 2, (Constants.HEIGHT - sideDimension) / 2, sideDimension, sideDimension);
			if (s.isFlipX())
				s.flip(true, false);
			s.draw(batch);
		}
	}

	public void reset(int swapWith) {
		moveElapsedTime = 0;
		moveEndTime = ANIMATION_TIME;

		this.swapWith = swapWith;
		if (model.selfTurn)
			model.showSelfCeldroid = false;
		else
			model.showOpponentCeldroid = false;
	}
}
