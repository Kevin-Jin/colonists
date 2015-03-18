package in.kevinj.colonists.world;

import in.kevinj.colonists.world.Coordinate;

public interface Entity {
	public enum Type {
		HIGHWAYMAN, ROAD, VILLAGE, METRO
	}

	public void update(float tDelta);
	public Type getType();

	public interface PositiveSpace extends Entity {
		public Coordinate.PositiveSpace getPosition();
		public void setPosition(Coordinate.PositiveSpace coord);
	}

	public interface Highwayman extends PositiveSpace {
		
	}

	public interface NegativeSpace extends Entity {
		public int getPlayer();
		public Coordinate.NegativeSpace getPosition();
		public void setPosition(Coordinate.NegativeSpace coord);
	}

	public interface Road extends NegativeSpace {
		
	}

	public interface Settlement extends NegativeSpace {
		
	}

	public interface Village extends Settlement {
		
	}

	public interface Metro extends Settlement {
		
	}
}
