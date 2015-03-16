package in.kevinj.colonists.world;

import java.util.Set;

public abstract class PlayerAction {
	public static class BeginConsiderMove extends PlayerAction {
		private final Coordinate.CoordinateType type;
		private final Coordinate coord;

		public BeginConsiderMove(GameMap<?> m, Coordinate.CoordinateType type, Coordinate coord) {
			super(m);
			this.type = type;
			this.coord = coord;
		}

		@Override
		public void update(float tDelta) {
			switch (type) {
				case TILE:
					model.setHighwaymanCandidate((Coordinate.PositiveSpace) coord);
					break;
				case VERTEX:
					model.setMetroCandidate((Coordinate.NegativeSpace) coord);
					break;
				case EDGE:
					model.setRoadCandidate((Coordinate.NegativeSpace) coord);
					break;
			}
		}
	}

	public static class EndConsiderMove extends PlayerAction {
		private final Coordinate.CoordinateType type;

		public EndConsiderMove(GameMap<?> m, Coordinate.CoordinateType type) {
			super(m);
			this.type = type;
		}

		@Override
		public void update(float tDelta) {
			switch (type) {
				case TILE:
					model.setHighwaymanCandidate(null);
					break;
				case VERTEX:
					model.setMetroCandidate(null);
					break;
				case EDGE:
					model.setRoadCandidate(null);
					break;
			}
		}
	}

	public static abstract class CommitMove<M extends GameMap<E>, E extends Entity.NegativeSpace> extends PlayerAction {
		protected M model;
		private final Coordinate.CoordinateType type;
		private final Coordinate coord;

		public CommitMove(M m, Coordinate.CoordinateType type, Coordinate coord) {
			super(m);
			this.model = m;
			this.type = type;
			this.coord = coord;
		}

		protected abstract E createMetro(M model, int player);

		protected abstract E createVillage(M model, int player);

		protected abstract E createRoad(M model, int player);

		@Override
		public void update(float tDelta) {
			Set<Coordinate.NegativeSpace> availableMoves = model.getPlayer(model.getCurrentPlayerTurn()).availableMoves;
			switch (type) {
				case TILE:
					model.getHighwayman().setPosition((Coordinate.PositiveSpace) coord);
					break;
				case VERTEX:
					if (model.removeFromGrid((Coordinate.NegativeSpace) coord) == null)
						if (availableMoves.contains((Coordinate.NegativeSpace) coord))
							model.addToGrid((Coordinate.NegativeSpace) coord, Math.random() < 0.5 ? createMetro(model, model.getCurrentPlayerTurn()) : createVillage(model, model.getCurrentPlayerTurn()));
					break;
				case EDGE:
					if (model.removeFromGrid((Coordinate.NegativeSpace) coord) == null)
						if (availableMoves.contains((Coordinate.NegativeSpace) coord))
							model.addToGrid((Coordinate.NegativeSpace) coord, createRoad(model, model.getCurrentPlayerTurn()));
					break;
			}
		}
	}

	protected GameMap<?> model;

	protected PlayerAction(GameMap<?> model) {
		this.model = model;
	}

	public abstract void update(float tDelta);
}
