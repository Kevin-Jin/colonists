package in.kevinj.colonists.client.world;

import in.kevinj.colonists.client.ViewComponent;
import in.kevinj.colonists.world.Coordinate;
import in.kevinj.colonists.world.Entity;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class GraphicalEntity implements Entity, ViewComponent {
	protected final WorldModel model;
	private final String sprite;
	protected Sprite spriteLoaded;

	public GraphicalEntity(WorldModel model, String sprite) {
		this.model = model;
		this.sprite = sprite;
	}

	@Override
	public void update(float tDelta) {
		
	}

	protected abstract void positionSprite();

	@Override
	public void draw(SpriteBatch batch) {
		if (spriteLoaded == null)
			spriteLoaded = model.parent.sprites.get(sprite);

		positionSprite();
		spriteLoaded.draw(batch);
	}

	public static abstract class PositiveSpace extends GraphicalEntity implements Entity.PositiveSpace {
		public Coordinate.PositiveSpace position;

		public PositiveSpace(WorldModel model, String sprite) {
			super(model, sprite);
		}

		@Override
		public Coordinate.PositiveSpace getPosition() {
			return position;
		}

		@Override
		public void setPosition(Coordinate.PositiveSpace coord) {
			position = coord;
		}
	}

	public static class Highwayman extends PositiveSpace implements Entity.Highwayman {
		private final float[] color;

		public Highwayman(WorldModel model) {
			super(model, "map/highwayman");
			color = new float[] { 1, 1, 1, 1 };
		}

		public Highwayman(WorldModel model, boolean greenHighlight) {
			super(model, "map/highwayman");
			if (greenHighlight)
				color = new float[] { 0, 1, 0, 0.8f };
			else
				color = new float[] { 1, 0, 0, 0.8f };
		}

		@Override
		protected void positionSprite() {
			spriteLoaded.setColor(color[0], color[1], color[2], color[3]);
			spriteLoaded.setPosition(model.tileWidth / 4 * 3 * position.x + spriteLoaded.getWidth() / 2, model.tileHeight * position.y - model.tileHeight / 2 * position.x + model.tileHeight + (model.tileHeight - spriteLoaded.getHeight()) / 2);
		}

		@Override
		public Type getType() {
			return Type.HIGHWAYMAN;
		}
	}

	public static abstract class NegativeSpace extends GraphicalEntity implements Entity.NegativeSpace {
		private static final float[][] PLAYER_COLORS = {
			{ 1.0f, 0.5f, 1.0f, 1.0f },
			{ 0.5f, 1.0f, 1.0f, 1.0f },
			{ 1.0f, 1.0f, 0.5f, 1.0f },
			{ 0.5f, 0.5f, 1.0f, 1.0f },
			{ 0.5f, 1.0f, 0.5f, 1.0f },
			{ 1.0f, 0.5f, 0.5f, 1.0f },
			{ 0.0f, 1.0f, 0.0f, 0.8f },
			{ 1.0f, 0.0f, 0.0f, 0.8f }
		};

		public final int player;
		public Coordinate.NegativeSpace position;

		public NegativeSpace(WorldModel model, int player, String sprite) {
			super(model, sprite);
			this.player = player;
		}

		@Override
		protected void positionSprite() {
			float[] color = PLAYER_COLORS[player];
			spriteLoaded.setColor(color[0], color[1], color[2], color[3]);
		}

		@Override
		public int getPlayer() {
			return player;
		}

		@Override
		public Coordinate.NegativeSpace getPosition() {
			return position;
		}

		@Override
		public void setPosition(Coordinate.NegativeSpace coord) {
			position = coord;
		}
	}

	public static class Road extends NegativeSpace implements Entity.Road {
		public Road(WorldModel model, int player) {
			super(model, player, "map/road");
		}

		public Road(WorldModel model, boolean greenHighlight) {
			this(model, greenHighlight ? 6 : 7);
		}

		@Override
		protected void positionSprite() {
			super.positionSprite();

			int[] info = position.getEdgeXYR(model.tileWidth, model.tileHeight);
			switch (info[2]) {
				case 0:
					info[1] -= spriteLoaded.getHeight() / 2;
					break;
				case 60:
					info[1] -= spriteLoaded.getHeight();
					break;
				case 120:
					info[0] += spriteLoaded.getHeight() / 2;
					break;
			}
			spriteLoaded.setPosition(info[0], info[1]);
			spriteLoaded.setRotation(info[2]);
			spriteLoaded.setOrigin(0, 0);
		}

		@Override
		public Type getType() {
			return Type.ROAD;
		}
	}

	private static abstract class Settlement extends NegativeSpace implements Entity.Settlement {
		public Settlement(WorldModel model, int player, String sprite) {
			super(model, player, sprite);
		}

		@Override
		protected void positionSprite() {
			super.positionSprite();

			int[] center = position.getVertexCenter(model.tileWidth, model.tileHeight);
			spriteLoaded.setPosition(center[0] - spriteLoaded.getWidth() / 2, center[1] - spriteLoaded.getHeight() / 2);
		}
	}

	public static class Village extends Settlement implements Entity.Village {
		public Village(WorldModel model, int player) {
			super(model, player, "map/village");
		}

		public Village(WorldModel model, boolean greenHighlight) {
			this(model, greenHighlight ? 6 : 7);
		}

		@Override
		public Type getType() {
			return Type.VILLAGE;
		}
	}

	public static class Metro extends Settlement implements Entity.Metro {
		public Metro(WorldModel model, int player) {
			super(model, player, "map/metro");
		}

		public Metro(WorldModel model, boolean greenHighlight) {
			this(model, greenHighlight ? 6 : 7);
		}

		@Override
		public Type getType() {
			return Type.METRO;
		}
	}
}
