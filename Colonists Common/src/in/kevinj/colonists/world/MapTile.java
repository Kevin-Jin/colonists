package in.kevinj.colonists.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Queue;
import java.util.Random;

public abstract class MapTile {
	public enum ResourceType {
		RICE(4), BAMBOO(4), HEMP(4), IRON(3), BROWNSTONE(3), WASTELAND(1);

		public final int frequency;

		ResourceType(int frequency) {
			this.frequency = frequency;
		}
	}

	public enum PortType {
		NONE(9), HEMP(1), RICE(1), BROWNSTONE(1), BAMBOO(1), IRON(1), PLAIN(4);

		public final int frequency;

		PortType(int frequency) {
			this.frequency = frequency;
		}
	}

	public static class ResourceTile extends MapTile {
		private static final int[] CHIT_ORDER = { 5, 2, 6, 3, 8, 10, 9, 12, 11, 4, 8, 10, 9, 4, 5, 6, 3, 11 };

		public final int chit;
		public final ResourceType type;

		public ResourceTile(int chit, ResourceType type) {
			this.chit = chit;
			this.type = type;
		}

		@Override
		public boolean isResource() {
			return true;
		}

		@Override
		public ResourceType getResourceType() {
			return type;
		}

		@Override
		public int getChit() {
			return chit;
		}

		@Override
		public PortType getPortType() {
			throw new UnsupportedOperationException();
		}

		@Override
		public float getRotation() {
			return 0;
		}

		public static void getRandomResources(Random r, Queue<MapTile> queue) {
			ArrayList<ResourceType> resources = new ArrayList<ResourceType>(19);

			for (ResourceType type : ResourceType.values())
				for (int i = type.frequency - 1; i >= 0; --i)
					resources.add(type);
			Collections.shuffle(resources, r);

			int j = resources.size() - 1;
			for (int i = 0; j >= 0; --j) {
				ResourceType type = resources.get(j);
				if (type == ResourceType.WASTELAND) {
					queue.add(new ResourceTile(0, type));
				} else {
					queue.add(new ResourceTile(CHIT_ORDER[i], type));
					i++;
				}
			}

			if (j != -1) throw new AssertionError("Did not use all 18 resources");
		}
	}

	public static class PortTile extends MapTile {
		public final PortType type;
		public final float rotation;

		public PortTile(PortType type, float rotation) {
			this.type = type;
			this.rotation = rotation;
		}

		@Override
		public boolean isResource() {
			return false;
		}

		@Override
		public ResourceType getResourceType() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getChit() {
			return 0;
		}

		@Override
		public PortType getPortType() {
			return type;
		}

		@Override
		public float getRotation() {
			return rotation;
		}

		public static void getRandomPorts(Random r, int rot, Queue<MapTile> queue) {
			ArrayList<PortType> ports = new ArrayList<PortType>(9);

			for (PortType type : PortType.values())
				for (int i = type.frequency - 1; i >= 0; --i)
					if (type != PortType.NONE)
						ports.add(type);
			Collections.shuffle(ports, r);

			//can have maximum of two ports that are separated by only one edge
			//from a neighbor.
			//can have maximum of two water tiles in a row.
			//must end up with 9 water tiles and 9 port tiles.
			int remainingStaggers = 2, remainingSpacers = 0, j = 0, consecutive = 1;
			boolean placePort = false;
			for (int i = 0; i < 18; i++) {
				if (placePort) {
					queue.add(new PortTile(ports.get(j), rot));
					j++;
					if (j != 9 && consecutive < 2 && remainingStaggers > 0 && r.nextInt(4) < 1) {
						remainingStaggers--;
						remainingSpacers++;
						consecutive++;
					} else {
						placePort = false;
						consecutive = 1;
					}
				} else {
					queue.add(new PortTile(PortType.NONE, rot));
					if (j == 9 || consecutive < 2 && remainingSpacers > 0 && r.nextInt(4) < 1) {
						remainingSpacers--;
						consecutive++;
					} else {
						placePort = true;
						consecutive = 1;
					}
				}
				if ((i + 2) % 3 == 0)
					rot = (rot + 60) % 360;
			}

			if (j != 9) throw new AssertionError("Did not use all 9 ports (" + j + " used)");
		}
	}

	public abstract boolean isResource();

	public abstract ResourceType getResourceType();

	public abstract int getChit();

	public abstract PortType getPortType();

	public abstract float getRotation();
}
