package net.pjtb.celdroids.client.battle;

import net.pjtb.celdroids.Constants;
import net.pjtb.celdroids.client.CeldroidBattleMove;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class AttackAnimation extends BattleAnimation {
	private static final float ANIMATION_TIME = 1.5f; //total time to show animation, in seconds
	private static final float FREEZE_TIME = 0.5f; //time out of total to have animation stay over dest, in seconds
	private static final float ANIMATION_FREQUENCY = 7; //in frames per second

	public CeldroidBattleMove move;

	public AttackAnimation(BattleModel model) {
		super(model);
		useTurn = true;
	}

	@Override
	public void draw(SpriteBatch batch) {
		Sprite s = model.parent.sprites.get(move.spriteDirectory + (int) (moveElapsedTime * ANIMATION_FREQUENCY) % move.frameCount);
		int destX, destY;
		if (move.selfOrigin && moveElapsedTime < (moveEndTime - FREEZE_TIME)) {
			int originX;
			int originY;
			if (model.selfTurn) {
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
			double xVel = (destX - originX) / (moveEndTime - FREEZE_TIME);
			double yVel = (destY - originY) / (moveEndTime - FREEZE_TIME);
			s.setX((float) (originX + xVel * moveElapsedTime));
			s.setY((float) (originY + yVel * moveElapsedTime));
		} else {
			if (model.selfTurn) {
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

	public void reset(CeldroidBattleMove move) {
		this.move = move;
		moveElapsedTime = 0;
		moveEndTime = move.loop ? ANIMATION_TIME : (move.frameCount / ANIMATION_FREQUENCY);
	}
}
