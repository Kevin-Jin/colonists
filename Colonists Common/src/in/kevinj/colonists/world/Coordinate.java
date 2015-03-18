package in.kevinj.colonists.world;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Coordinate {
	public enum Type {
		TILE, VERTEX, EDGE
	}

	public abstract Type getType();
	public abstract List<PositiveSpace> adjacentTiles();
	public abstract List<NegativeSpace> adjacentEdges();
	public abstract List<NegativeSpace> adjacentVertices();
	public abstract boolean inBounds();

	public static class PositiveSpace extends Coordinate implements Comparable<PositiveSpace> {
		private static Map<Short, PositiveSpace> cache = Collections.synchronizedMap(new HashMap<Short, PositiveSpace>());
	
		public byte x, y;
	
		private PositiveSpace(int x, int y) {
			this.x = (byte) x;
			this.y = (byte) y;
		}
	
		public byte getZ() {
			return (byte) (-x - y);
		}
	
		@Override
		public int compareTo(PositiveSpace other) {
			return this.hashCode() - other.hashCode();
		}
	
		@Override
		public boolean equals(Object b) {
			if (b instanceof PositiveSpace) {
				PositiveSpace other = (PositiveSpace) b;
				return other == this || other.x == this.x && other.y == this.y;
			}
			return false;
		}
	
		private static int hashCode(int x, int y) {
			return (x & 0xFF) << 8 | y & 0xFF;
		}
	
		@Override
		public int hashCode() {
			return hashCode(x, y);
		}
	
		@Override
		public String toString() {
			return "PositiveSpace[" + x + "," + y + "," + getZ() + "]";
		}

		@Override
		public Type getType() {
			return Type.TILE;
		}

		@Override
		public List<PositiveSpace> adjacentTiles() {
			List<PositiveSpace> list = new ArrayList<PositiveSpace>(6);
			list.add(valueOf(x + 0, y + 1));
			list.add(valueOf(x + 1, y + 1));
			list.add(valueOf(x + 1, y + 0));
			list.add(valueOf(x + 0, y - 1));
			list.add(valueOf(x - 1, y - 1));
			list.add(valueOf(x - 1, y + 0));
			return list;
		}

		@Override
		public List<NegativeSpace> adjacentEdges() {
			List<NegativeSpace> list = null;
			return list;
		}

		@Override
		public List<NegativeSpace> adjacentVertices() {
			List<NegativeSpace> list = new ArrayList<NegativeSpace>(6);
			list.add(NegativeSpace.valueOf(x * 100 + 0,		y * 100 + 100));
			list.add(NegativeSpace.valueOf(x * 100 + 100,	y * 100 + 150));
			list.add(NegativeSpace.valueOf(x * 100 + 100,	y * 100 + 100));
			list.add(NegativeSpace.valueOf(x * 100 + 100,	y * 100 + 50));
			list.add(NegativeSpace.valueOf(x * 100 + 0,		y * 100 + 0));
			list.add(NegativeSpace.valueOf(x * 100 + 0,		y * 100 + 50));
			return list;
		}

		@Override
		public boolean inBounds() {
			return x >= 1 && x <= 5 && y >= 1 && y <= 5
					&& (x > 3 || y <= x + 2)
					&& (x <= 3 || y >= x - 2);
		}
	
		public static PositiveSpace valueOf(int x, int y) {
			Short key = Short.valueOf((short) hashCode(x, y));
			PositiveSpace value = cache.get(key);
			if (value == null) {
				value = new PositiveSpace(x, y);
				cache.put(key, value);
			}
			return value;
		}
	}

	/**
	 * Takes advantage of the fact that regular triangles are the dual
	 * polyhedron of regular hexagons. We use two triangles stacked on top of
	 * each other for each x, y in the hexagonal grid to make it easier to
	 * find adjacent resource Tiles. Vertices are referenced by the coordinates
	 * of the triangle. Edges are referenced by the midpoint of the two
	 * vertices they connect.
	 */
	public static class NegativeSpace extends Coordinate implements Comparable<NegativeSpace> {
		private static final DecimalFormat FMT = new DecimalFormat("0.00");
	
		private static Map<Integer, NegativeSpace> cache = Collections.synchronizedMap(new HashMap<Integer, NegativeSpace>());

		public final byte x, y;
		public final byte xHundredths, yHundredths;

		private NegativeSpace(byte[] xNorm, byte[] yNorm) {
			this.x = xNorm[0];
			this.xHundredths = xNorm[1];
			this.y = yNorm[0];
			this.yHundredths = yNorm[1];
		}

		private static byte[] normalize(int a, int aHundredths) {
			if (aHundredths >= 0 && aHundredths < 100)
				return new byte[] { (byte) a, (byte) aHundredths };

			//make sure aHundreths is not negative
			a -= 1;
			aHundredths += 100;
			//integer division and modulus
			a += aHundredths / 100;
			aHundredths %= 100;
			return new byte[] { (byte) a, (byte) aHundredths };
		}

		public int xHundreds() {
			return x * 100 + xHundredths;
		}

		public int yHundreds() {
			return y * 100 + yHundredths;
		}

		@Override
		public Type getType() {
			if (xHundredths == 0 && yHundredths == 25 || xHundredths == 0 && yHundredths == 75 || xHundredths == 50 && yHundredths == 25)
				return Type.EDGE;
			if (xHundredths == 0 && yHundredths == 0 || xHundredths == 0 && yHundredths == 50)
				return Type.VERTEX;
			return null;
		}

		@Override
		public List<PositiveSpace> adjacentTiles() {
			List<PositiveSpace> list = null;
			Type type = getType();
			if (type == Type.VERTEX) {
				list = new ArrayList<PositiveSpace>(3);
				list.add(PositiveSpace.valueOf(x, (y * 100 + xHundredths - 50) / 100));
				list.add(PositiveSpace.valueOf(x - 1, y - 1));
				list.add(PositiveSpace.valueOf((x * 100 - xHundredths) / 100, y));
			}
			return list;
		}

		@Override
		public List<NegativeSpace> adjacentEdges() {
			List<NegativeSpace> list = null;
			Type type = getType();
			if (type == Type.EDGE) {
				list = new ArrayList<NegativeSpace>(4);
				//up [yHundredths == 25]/down [yHundredths == 75] left
				list.add(valueOf(x, xHundredths - 50, y, 25));
				//up right [yHundredths == 25]/left [yHundredths == 75]
				list.add(valueOf(x, 2 * xHundredths, y, yHundredths + 50));
				//down [yHundredths == 25]/up [yHundredths == 75] right
				list.add(valueOf(x, xHundredths + 50, y, 2 * yHundredths - 25));
				//down left [yHundredths == 25]/right [yHundredths == 75]
				list.add(valueOf(x, 0, y, yHundredths - 50));
			} else if (type == Type.VERTEX) {
				list = new ArrayList<NegativeSpace>(3);
				int right = -(int) Math.signum(yHundredths - 25);
				//down left [yHundredths == 0]/right [yHundredths == 50]
				list.add(valueOf(x, 0, y, yHundredths - 25));
				//right [yHundredths == 0]/left [yHundredths == 50]
				list.add(valueOf(x, xHundredths + 50 * right, y, yHundredths + 25 * right));
				//up left [yHundredths == 0]/right [yHundredths == 50]
				list.add(valueOf(x, xHundredths, y, yHundredths + 25));
			}
			return list;
		}

		@Override
		public List<NegativeSpace> adjacentVertices() {
			List<NegativeSpace> list = null;
			Type type = getType();
			if (type == Type.EDGE) {
				list = new ArrayList<NegativeSpace>(2);
				list.add(valueOf(x, (xHundredths - 50) / 100 * 100, y, yHundredths - 25));
				list.add(valueOf(x, (xHundredths + 50) / 100 * 100, y, yHundredths + 25));
			} else if (type == Type.VERTEX) {
				list = new ArrayList<NegativeSpace>(3);
				int right = -(int) Math.signum(yHundredths - 25);
				//right [yHundredths == 0]/left [yHundredths == 50]
				list.add(valueOf(x + right, xHundredths, y, yHundredths + 50 * right));
				//up
				list.add(valueOf(x, xHundredths, y, yHundredths + 50));
				//down
				list.add(valueOf(x, xHundredths, y, yHundredths - 50));
			}
			return list;
		}

		@Override
		public boolean inBounds() {
			Type type = getType();
			if (type == Type.EDGE)
				return x >= 1 && xHundreds() <= 600 && yHundreds() >= 125 && yHundreds() <= 625
						&& (x > 3 || yHundreds() <= xHundreds() + 275)
						&& (x <= 3 || yHundreds() >= xHundreds() - 225);
			else if (type == Type.VERTEX)
				return x >= 1 && x <= 6 && y >= 1 && y <= 6
						&& (x > 3 || yHundreds() <= xHundreds() + 300)
						&& (x <= 3 || yHundreds() >= xHundreds() - 250);
			else
				return false;
		}

		public static Set<NegativeSpace> allVertices() {
			Set<NegativeSpace> set = new HashSet<NegativeSpace>();
			for (int x = 100; x <= 300; x += 100)
				for (int y = x + 300; y >= 100; y -= 50)
					set.add(valueOf(x, y));
			for (int x = 400; x <= 600; x += 100)
				for (int y = x - 250; y <= 650; y += 50)
					set.add(valueOf(x, y));
			return set;
		}

		public int[] getVertexCenter(int tileWidth, int tileHeight) {
			if (getType() != Type.VERTEX) return null;

			if (yHundredths == 50) {
				return new int[] {
					tileWidth / 4 * 3 * x,
					tileHeight * (y + 1) - tileHeight / 2 * (x - 1)
				};
			} else if (yHundredths == 0) {
				return new int[] {
					tileWidth / 4 * 3 * x + tileWidth / 4 * 3 - tileWidth / 2,
					tileHeight * (y + 1) - tileHeight / 2 * x
				};
			} else {
				return null;
			}
		}

		public int[] getEdgeXYR(int tileWidth, int tileHeight) {
			if (getType() != Type.EDGE) return null;

			if (xHundredths == 50) {
				//horizontal
				return new int[] {
					tileWidth / 4 * 3 * x + tileWidth / 4 * 3 - tileWidth / 2,
					tileHeight * (y + 1) - tileHeight / 2 * x,
					0
				};
			} else if (yHundredths == 25) {
				//negative slope
				return new int[] {
					tileWidth / 4 * 3 * x + tileWidth / 4 * 3 - tileWidth / 2,
					tileHeight * (y + 1) - tileHeight / 2 * x,
					120
				};
			} else if (yHundredths == 75) {
				//positive slope
				return new int[] {
					tileWidth / 4 * 3 * x,
					tileHeight * (y + 1) - tileHeight / 2 * (x - 1),
					60
				};
			} else {
				return null;
			}
		}

		@Override
		public int compareTo(NegativeSpace other) {
			return this.hashCode() - other.hashCode();
		}

		@Override
		public boolean equals(Object b) {
			if (b instanceof NegativeSpace) {
				NegativeSpace other = (NegativeSpace) b;
				return other == this || other.x == this.x && other.xHundredths == this.xHundredths && other.y == this.y && other.yHundredths == this.yHundredths;
			}
			return false;
		}

		private static int hashCode(int x, int xHundredths, int y, int yHundredths) {
			return (x & 0xFF) << 24 | (xHundredths & 0xFF) << 16 | (y & 0xFF) << 8 | yHundredths & 0xFF;
		}

		@Override
		public int hashCode() {
			return hashCode(x, xHundredths, y, yHundredths);
		}

		@Override
		public String toString() {
			return "NegativeSpace[" + FMT.format(x + xHundredths / 100f) +"," + FMT.format(y + yHundredths / 100f) + "]";
		}

		public static NegativeSpace connection(NegativeSpace vertex1, NegativeSpace vertex2) {
			//the edge linking the two vertices is the midpoint
			if (vertex1 == null || vertex2 == null || vertex1.getType() != Type.VERTEX || vertex2.getType() != Type.VERTEX || !vertex1.adjacentVertices().contains(vertex2))
				return null;
			int xHundreds = (vertex1.xHundreds() + vertex2.xHundreds()) / 2;
			int yHundreds = (vertex1.yHundreds() + vertex2.yHundreds()) / 2;
			NegativeSpace edge = valueOf(xHundreds, yHundreds);
			assert edge.getType() == Type.EDGE;
			return edge;
		}

		public static NegativeSpace intersection(NegativeSpace edge1, NegativeSpace edge2) {
			//find the common vertex between the two edges
			if (edge1 == null || edge2 == null || edge1.getType() != Type.EDGE || edge2.getType() != Type.EDGE || !edge1.adjacentEdges().contains(edge2))
				return null;
			int xHundreds = (edge1.xHundredths == 0 ? edge1.xHundreds() : edge2.xHundreds());
			int yHundreds = (edge1.yHundreds() + edge2.yHundreds()) / 2;
			if (edge1.yHundreds() == edge2.yHundreds()) {
				if (edge1.xHundreds() == xHundreds) {
					if (edge2.xHundreds() < xHundreds)
						yHundreds += 25;
					else
						yHundreds -= 25;
				} else {
					assert edge2.xHundreds() == xHundreds;
					if (edge1.xHundreds() < xHundreds)
						yHundreds += 25;
					else
						yHundreds -= 25;
				}
			}
			NegativeSpace vertex = valueOf(xHundreds, yHundreds);
			assert vertex.getType() == Type.VERTEX;
			return vertex;
		}

		public static NegativeSpace valueOf(int x, int xHundredths, int y, int yHundredths) {
			byte[] xNorm = normalize(x, xHundredths);
			byte[] yNorm = normalize(y, yHundredths);
			Integer key = Integer.valueOf(hashCode(xNorm[0], xNorm[1], yNorm[0], yNorm[1]));
			NegativeSpace value = cache.get(key);
			if (value == null) {
				value = new NegativeSpace(xNorm, yNorm);
				cache.put(key, value);
			}
			return value;
		}

		public static NegativeSpace valueOf(int xHundreds, int yHundreds) {
			return valueOf(xHundreds / 100, xHundreds % 100, yHundreds / 100, yHundreds % 100);
		}
	}
}
