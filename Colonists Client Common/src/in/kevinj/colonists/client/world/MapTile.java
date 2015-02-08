package in.kevinj.colonists.client.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public abstract class MapTile {
	public enum ResourceType {
		RICE(4), BAMBOO(4), HEMP(4), IRON(3), STONE(3), WASTELAND(1);

		public final int frequency;

		ResourceType(int frequency) {
			this.frequency = frequency;
		}
	}

	public enum PortType {
		NONE, HEMP, RICE, STONE, BAMBOO, IRON
	}

	public static class ResourceTile extends MapTile {
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

		public static Queue<ResourceTile> getRandomTiles(Random r) {
			LinkedList<ResourceTile> queue = new LinkedList<ResourceTile>();
			ArrayList<Integer> chits = new ArrayList<Integer>(18);
			ArrayList<ResourceType> resources = new ArrayList<ResourceType>(19);

			chits.add(Integer.valueOf(2));
			for (int i = 3; i < 7; i++) {
				chits.add(Integer.valueOf(i));
				chits.add(Integer.valueOf(i));
			}
			for (int i = 8; i < 12; i++) {
				chits.add(Integer.valueOf(i));
				chits.add(Integer.valueOf(i));
			}
			chits.add(Integer.valueOf(12));
			Collections.shuffle(chits, r);

			for (ResourceType type : ResourceType.values())
				for (int i = type.frequency - 1; i >= 0; --i)
					resources.add(type);
			Collections.shuffle(resources, r);

			for (int i = chits.size() - 1, j = resources.size() - 1; j >= 0; --j) {
				ResourceType type = resources.get(j);
				if (type == ResourceType.WASTELAND) {
					queue.add(new ResourceTile(0, type));
				} else {
					queue.add(new ResourceTile(chits.get(i).intValue(), type));
					--i;
				}
			}
			return queue;
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
	}

	public abstract boolean isResource();

	public abstract ResourceType getResourceType();

	public abstract int getChit();

	public abstract PortType getPortType();

	public abstract float getRotation();
}
