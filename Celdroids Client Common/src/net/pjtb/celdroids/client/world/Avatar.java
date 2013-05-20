package net.pjtb.celdroids.client.world;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import net.pjtb.celdroids.client.ViewComponent;

public class Avatar implements ViewComponent {
	private static final float VELOCITY = 5; //tiles per second
	private static final float ANIMATION_FREQUENCY = 6; //walking animation, in frames per second
	private static final float CHANGE_DIRECTION_DELAY = 0.25f; //in seconds

	private final WorldModel model;

	private DirectionalPad.State dirInProgress;
	private double posX, posY;
	private String sprite;
	private boolean flip;
	private double timeSinceStart;
	private long stillTimeEnd;

	public Avatar(WorldModel model) {
		this.model = model;

		dirInProgress = DirectionalPad.State.NONE;
		sprite = "character/down0";
	}

	@Override
	public void update(float tDelta) {
		if (dirInProgress == DirectionalPad.State.NONE)
			dirInProgress = model.dpad.state;
		else
			timeSinceStart += tDelta;

		switch (dirInProgress) {
			case UP: {
				boolean changedDirection = (sprite.length() != "character/up".length() + 1 || !sprite.substring(0, "character/up".length()).equals("character/up") || flip);
				sprite = "character/up";
				flip = false;
				long now = System.currentTimeMillis();
				if (changedDirection)
					stillTimeEnd = now + (int) ((CHANGE_DIRECTION_DELAY - timeSinceStart) * 1000);
				if (now < stillTimeEnd) {
					dirInProgress = DirectionalPad.State.NONE;
					timeSinceStart = 0;
					sprite += "0";
				} else {
					double unclipped = posY + VELOCITY * tDelta, endAt;
					if (dirInProgress == model.dpad.state || unclipped < (endAt = Math.floor(posY + 1))) {
						posY = unclipped;
						sprite += (int) (timeSinceStart * ANIMATION_FREQUENCY) % 2 + 1;
					} else {
						posY = endAt;
						dirInProgress = DirectionalPad.State.NONE;
						timeSinceStart = 0;
						sprite += "0";
					}
				}
				break;
			}
			case RIGHT: {
				boolean changedDirection = (sprite.length() != "character/left".length() + 1 || !sprite.substring(0, "character/left".length()).equals("character/left") || !flip);
				sprite = "character/left";
				flip = true;
				long now = System.currentTimeMillis();
				if (changedDirection)
					stillTimeEnd = now + (int) ((CHANGE_DIRECTION_DELAY - timeSinceStart) * 1000);
				if (now < stillTimeEnd) {
					dirInProgress = DirectionalPad.State.NONE;
					timeSinceStart = 0;
					sprite += "0";
				} else {
					double unclipped = posX + VELOCITY * tDelta, endAt;
					if (dirInProgress == model.dpad.state || unclipped < (endAt = Math.floor(posX + 1))) {
						posX = unclipped;
						sprite += (int) (timeSinceStart * ANIMATION_FREQUENCY) % 2 + 1;
					} else {
						posX = endAt;
						dirInProgress = DirectionalPad.State.NONE;
						timeSinceStart = 0;
						sprite += "0";
					}
				}
				break;
			}
			case DOWN: {
				boolean changedDirection = (sprite.length() != "character/down".length() + 1 || !sprite.substring(0, "character/down".length()).equals("character/down") || flip);
				sprite = "character/down";
				flip = false;
				long now = System.currentTimeMillis();
				if (changedDirection)
					stillTimeEnd = now + (int) ((CHANGE_DIRECTION_DELAY - timeSinceStart) * 1000);
				if (now < stillTimeEnd) {
					dirInProgress = DirectionalPad.State.NONE;
					timeSinceStart = 0;
					sprite += "0";
				} else {
					double unclipped = posY - VELOCITY * tDelta, endAt;
					if (dirInProgress == model.dpad.state || unclipped > (endAt = Math.ceil(posY - 1))) {
						posY = unclipped;
						sprite += (int) (timeSinceStart * ANIMATION_FREQUENCY) % 2 + 1;
					} else {
						posY = endAt;
						dirInProgress = DirectionalPad.State.NONE;
						timeSinceStart = 0;
						sprite += "0";
					}
				}
				break;
			}
			case LEFT: {
				boolean changedDirection = (sprite.length() != "character/left".length() + 1 || !sprite.substring(0, "character/left".length()).equals("character/left") || flip);
				sprite = "character/left";
				flip = false;
				long now = System.currentTimeMillis();
				if (changedDirection)
					stillTimeEnd = now + (int) ((CHANGE_DIRECTION_DELAY - timeSinceStart) * 1000);
				if (now < stillTimeEnd) {
					dirInProgress = DirectionalPad.State.NONE;
					timeSinceStart = 0;
					sprite += "0";
				} else {
					double unclipped = posX - VELOCITY * tDelta, endAt;
					if (dirInProgress == model.dpad.state || unclipped > (endAt = Math.ceil(posX - 1))) {
						posX = unclipped;
						sprite += (int) (timeSinceStart * ANIMATION_FREQUENCY) % 2 + 1;
					} else {
						posX = endAt;
						dirInProgress = DirectionalPad.State.NONE;
						timeSinceStart = 0;
						sprite += "0";
					}
				}
				break;
			}
			case NONE:
				break;
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		Sprite s = model.parent.sprites.get(sprite);
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 12; j++) {
				s.setBounds(i * 60 + (int) (posX * 60), j * 60 + (int) (posY * 60), 60, 60);
				if (s.isFlipX() != flip)
					s.flip(true, false);
				s.draw(batch);
			}
		}
	}
}
