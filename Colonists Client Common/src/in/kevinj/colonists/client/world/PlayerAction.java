package in.kevinj.colonists.client.world;

import java.util.Set;

public abstract class PlayerAction {
	public static class BeginConsiderMove extends PlayerAction {
		private final Coordinate.CoordinateType type;
		private final Coordinate coord;

		public BeginConsiderMove(WorldModel m, Coordinate.CoordinateType type, Coordinate coord) {
			super(m);
			this.type = type;
			this.coord = coord;
		}

		@Override
		public void update(float tDelta) {
			switch (type) {
				case TILE:
					model.highwaymanCandidate = (Coordinate.PositiveSpace) coord;
					break;
				case VERTEX:
					model.metroCandidate = (Coordinate.NegativeSpace) coord;
					break;
				case EDGE:
					model.roadCandidate = (Coordinate.NegativeSpace) coord;
					break;
			}
		}
	}

	public static class EndConsiderMove extends PlayerAction {
		private final Coordinate.CoordinateType type;

		public EndConsiderMove(WorldModel m, Coordinate.CoordinateType type) {
			super(m);
			this.type = type;
		}

		@Override
		public void update(float tDelta) {
			switch (type) {
				case TILE:
					model.highwaymanCandidate = null;
					break;
				case VERTEX:
					model.metroCandidate = null;
					break;
				case EDGE:
					model.roadCandidate = null;
					break;
			}
		}
	}

	public static class CommitMove extends PlayerAction {
		private final Coordinate.CoordinateType type;
		private final Coordinate coord;

		protected CommitMove(WorldModel m, Coordinate.CoordinateType type, Coordinate coord) {
			super(m);
			this.type = type;
			this.coord = coord;
		}

		@Override
		public void update(float tDelta) {
			Set<Coordinate.NegativeSpace> availableMoves = model.getPlayer(model.getCurrentPlayerTurn()).availableMoves;
			switch (type) {
				case TILE:
					model.highwayman.position = (Coordinate.PositiveSpace) coord;
					break;
				case VERTEX:
					if (model.removeFromGrid((Coordinate.NegativeSpace) coord) == null)
						if (availableMoves.contains((Coordinate.NegativeSpace) coord))
							model.addToGrid((Coordinate.NegativeSpace) coord, Math.random() < 0.5 ? new Entity.Metro(model, model.getCurrentPlayerTurn()) : new Entity.Village(model, model.getCurrentPlayerTurn()));
					break;
				case EDGE:
					if (model.removeFromGrid((Coordinate.NegativeSpace) coord) == null)
						if (availableMoves.contains((Coordinate.NegativeSpace) coord))
							model.addToGrid((Coordinate.NegativeSpace) coord, new Entity.Road(model, model.getCurrentPlayerTurn()));
					break;
			}
		}
	}

	protected WorldModel model;

	protected PlayerAction(WorldModel model) {
		this.model = model;
	}

	public abstract void update(float tDelta);
}
