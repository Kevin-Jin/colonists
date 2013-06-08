package net.pjtb.celdroids.client.battle;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import net.pjtb.celdroids.client.ViewComponent;

public abstract class BattleAnimation implements ViewComponent {
	protected final BattleModel model;

	protected float moveElapsedTime;
	protected float moveEndTime;

	public BattleAnimation(BattleModel model) {
		this.model = model;
	}

	public void update(float tDelta) {
		moveElapsedTime += tDelta;
		if (moveElapsedTime > moveEndTime) {
			model.currentAnimation = null;
			if (model.selfTurn) {
				model.canAct = false;
				model.selfTurn = false;
			} else {
				model.canAct = true;
				model.selfTurn = true;
			}
		}
	}

	@Override
	public abstract void draw(SpriteBatch batch);
}
