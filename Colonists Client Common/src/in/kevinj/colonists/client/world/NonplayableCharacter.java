package in.kevinj.colonists.client.world;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class NonplayableCharacter implements Entity {
	private static final float VELOCITY = 3; // tiles per second
	private static final float ANIMATION_FREQUENCY = 7; // walking animation, in frames per second
	private static final float CHANGE_DIRECTION_DELAY = 0.25f; // in seconds

	private final WorldModel model;

	private final String spritePathPrefix;
	public final String trainerProps;

	private DirectionalPad.State dirInProgress;
	private double posX, posY;
	private String sprite;
	private boolean flip;
	private double timeSinceStart;
	private long stillTimeEnd;

	private boolean collided;

	public NonplayableCharacter(WorldModel model, int posX, int posY, String trainerProps) {
		this.model = model;
		this.posX = posX;
		this.posY = posY;

		spritePathPrefix = "character/human2/";
		dirInProgress = DirectionalPad.State.NONE;
		sprite = spritePathPrefix + "down/0";

		this.trainerProps = trainerProps;
	}

	@Override
	public void update(float tDelta) {
		/*if (dirInProgress == DirectionalPad.State.NONE)
			dirInProgress = model.dpad.state;
		else
			timeSinceStart += tDelta;*/

		if (collided) {
			if (dirInProgress == DirectionalPad.State.NONE) {
				timeSinceStart = 0;
				sprite = sprite.substring(0, sprite.lastIndexOf('/') + 1) + "0";
			} else {
				// make sure stillTimeEnd will not get messed when changing direction after a collision
				String direction = sprite.substring(spritePathPrefix.length(), sprite.lastIndexOf('/'));
				if (direction.equals("left") && !flip && dirInProgress == DirectionalPad.State.LEFT
						|| direction.equals("left") && flip && dirInProgress == DirectionalPad.State.RIGHT
						|| direction.equals("up") && dirInProgress == DirectionalPad.State.UP
						|| direction.equals("down") && dirInProgress == DirectionalPad.State.DOWN)
					timeSinceStart += tDelta;
				else
					timeSinceStart = 0;
			}
			collided = false;
		}

		switch (dirInProgress) {
			case UP: {
				String spritePath = spritePathPrefix + "up/";
				boolean changedDirection = (!sprite.substring(0, sprite.lastIndexOf('/') + 1).equals(spritePath) || flip);
				sprite = spritePath;
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
					collided = collision(posX, unclipped);
					if (collided) {
						endAt = Math.ceil(posY);
						updateAvatarInGrid(posX, endAt);
						posY = endAt;
						dirInProgress = DirectionalPad.State.NONE;
						sprite += (int) (timeSinceStart * ANIMATION_FREQUENCY) % 4;
					} else if (dirInProgress == model.dpad.state || unclipped < (endAt = Math.floor(posY + 1))) {
						updateAvatarInGrid(posX, unclipped);
						posY = unclipped;
						sprite += (int) (timeSinceStart * ANIMATION_FREQUENCY) % 4;
					} else {
						updateAvatarInGrid(posX, endAt);
						posY = endAt;
						dirInProgress = DirectionalPad.State.NONE;
						timeSinceStart = 0;
						sprite += "0";
					}
				}
				break;
			}
			case RIGHT: {
				String spritePath = spritePathPrefix + "left/";
				boolean changedDirection = (!sprite.substring(0, sprite.lastIndexOf('/') + 1).equals(spritePath) || !flip);
				sprite = spritePath;
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
					collided = collision(unclipped, posY);
					if (collided) {
						endAt = Math.ceil(posX);
						updateAvatarInGrid(endAt, posY);
						posX = endAt;
						dirInProgress = DirectionalPad.State.NONE;
						sprite += (int) (timeSinceStart * ANIMATION_FREQUENCY) % 4;
					} else if (dirInProgress == model.dpad.state || unclipped < (endAt = Math.floor(posX + 1))) {
						updateAvatarInGrid(unclipped, posY);
						posX = unclipped;
						sprite += (int) (timeSinceStart * ANIMATION_FREQUENCY) % 4;
					} else {
						updateAvatarInGrid(endAt, posY);
						posX = endAt;
						dirInProgress = DirectionalPad.State.NONE;
						timeSinceStart = 0;
						sprite += "0";
					}
				}
				break;
			}
			case DOWN: {
				String spritePath = spritePathPrefix + "down/";
				boolean changedDirection = (!sprite.substring(0, sprite.lastIndexOf('/') + 1).equals(spritePath) || flip);
				sprite = spritePath;
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
					collided = collision(posX, unclipped);
					if (collided) {
						endAt = Math.floor(posY);
						updateAvatarInGrid(posX, endAt);
						posY = endAt;
						dirInProgress = DirectionalPad.State.NONE;
						sprite += (int) (timeSinceStart * ANIMATION_FREQUENCY) % 4;
					} else if (dirInProgress == model.dpad.state || unclipped > (endAt = Math.ceil(posY - 1))) {
						updateAvatarInGrid(posX, unclipped);
						posY = unclipped;
						sprite += (int) (timeSinceStart * ANIMATION_FREQUENCY) % 4;
					} else {
						updateAvatarInGrid(posX, endAt);
						posY = endAt;
						dirInProgress = DirectionalPad.State.NONE;
						timeSinceStart = 0;
						sprite += "0";
					}
				}
				break;
			}
			case LEFT: {
				String spritePath = spritePathPrefix + "left/";
				boolean changedDirection = (!sprite.substring(0, sprite.lastIndexOf('/') + 1).equals(spritePath) || flip);
				sprite = spritePath;
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
					collided = collision(unclipped, posY);
					if (collided) {
						endAt = Math.floor(posX);
						updateAvatarInGrid(endAt, posY);
						posX = endAt;
						dirInProgress = DirectionalPad.State.NONE;
						sprite += (int) (timeSinceStart * ANIMATION_FREQUENCY) % 4;
					} else if (dirInProgress == model.dpad.state || unclipped > (endAt = Math.ceil(posX - 1))) {
						updateAvatarInGrid(unclipped, posY);
						posX = unclipped;
						sprite += (int) (timeSinceStart * ANIMATION_FREQUENCY) % 4;
					} else {
						updateAvatarInGrid(endAt, posY);
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

	public boolean isStationary() {
		return dirInProgress == DirectionalPad.State.NONE;
	}

	private Coordinate getNextLocation(double posX, double posY) {
		String direction = sprite.substring(spritePathPrefix.length(), sprite.lastIndexOf('/'));
		if (direction.equals("left"))
			if (flip)
				return new Coordinate((int) Math.floor(posX + 1), (int) posY);
			else
				return new Coordinate((int) Math.ceil(posX - 1), (int) posY);
		if (direction.equals("up"))
			return new Coordinate((int) posX, (int) Math.floor(posY + 1));
		if (direction.equals("down"))
			return new Coordinate((int) posX, (int) Math.ceil(posY - 1));
		return null;
	}

	private boolean collision(double posXfinal, double posYfinal) {
		Coordinate loc = getNextLocation(posXfinal, posYfinal);
		if (loc.row < 0 || loc.col < 0 || loc.row >= model.mapBoundsRows || loc.col >= model.mapBoundsColumns)
			return true;
		Entity ent = model.grid[loc.row][loc.col];
		return (ent != null && ent != this);
	}

	public Coordinate getNextLocation() {
		return getNextLocation(posX, posY);
	}

	private void updateAvatarInGrid(double posXfinal, double posYfinal) {
		if (posX == posXfinal) {
			int indexX = (int) posX;
			model.grid[(int) Math.ceil(posY)][indexX] = null;
			model.grid[(int) Math.floor(posY)][indexX] = null;
			model.grid[(int) Math.ceil(posYfinal)][indexX] = this;
			model.grid[(int) Math.floor(posYfinal)][indexX] = this;
		} else if (posY == posYfinal) {
			int indexY = (int) posY;
			model.grid[indexY][(int) Math.ceil(posX)] = null;
			model.grid[indexY][(int) Math.floor(posX)] = null;
			model.grid[indexY][(int) Math.ceil(posXfinal)] = this;
			model.grid[indexY][(int) Math.floor(posXfinal)] = this;
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		Sprite s = model.parent.sprites.get(sprite);
		s.setBounds((float) (posX * WorldModel.TILE_SIZE), (float) (posY * WorldModel.TILE_SIZE), WorldModel.TILE_SIZE, WorldModel.TILE_SIZE);
		if (s.isFlipX() != flip)
			s.flip(true, false);
		s.draw(batch);
	}
}
