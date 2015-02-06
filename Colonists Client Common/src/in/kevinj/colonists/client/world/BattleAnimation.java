package in.kevinj.colonists.client.world;

import in.kevinj.colonists.client.ViewComponent;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class BattleAnimation implements ViewComponent {
	protected final BattleModel model;

	protected float moveElapsedTime;
	protected float moveEndTime;

	public boolean useTurn;

	public BattleAnimation(BattleModel model) {
		this.model = model;
	}

	@Override
	public void update(float tDelta) {
		moveElapsedTime += tDelta;
		if (moveElapsedTime > moveEndTime) {
			// animation over
			model.currentAnimation = null;
			if (useTurn) {
				model.selfTurn = !model.selfTurn;
				model.canAct = model.selfTurn;
			}
		}
	}

	@Override
	public abstract void draw(SpriteBatch batch);
}
