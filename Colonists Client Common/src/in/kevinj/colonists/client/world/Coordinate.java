package in.kevinj.colonists.client.world;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Coordinate {
	public enum CoordinateType {
		TILE, VERTEX, EDGE
	}

	public static class PositiveSpace extends Coordinate implements Comparable<Coordinate.PositiveSpace> {
		private static Map<Short, Coordinate.PositiveSpace> cache = Collections.synchronizedMap(new HashMap<Short, Coordinate.PositiveSpace>());
	
		public byte x, y;
	
		private PositiveSpace(int x, int y) {
			this.x = (byte) x;
			this.y = (byte) y;
		}
	
		public byte getZ() {
			return (byte) (-x - y);
		}
	
		@Override
		public int compareTo(Coordinate.PositiveSpace other) {
			return this.hashCode() - other.hashCode();
		}
	
		@Override
		public boolean equals(Object b) {
			if (b instanceof Coordinate.PositiveSpace) {
				Coordinate.PositiveSpace other = (Coordinate.PositiveSpace) b;
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
			return "Coordinate.PositiveSpace[" + x + "," + y + "," + getZ() + "]";
		}
	
		public static Coordinate.PositiveSpace valueOf(int x, int y) {
			Short key = Short.valueOf((short) hashCode(x, y));
			Coordinate.PositiveSpace value = cache.get(key);
			if (value == null) {
				value = new Coordinate.PositiveSpace(x, y);
				cache.put(key, value);
			}
			return value;
		}
	}

	/**
	 * Takes advantage of the fact that regular triangles are the dual
	 * polyhedron of regular hexagons. We use two triangles stacked on top of
	 * each other for each x, y in the hexagonal grid to make it easier to
	 * find adjacent resource Coordinate.Tiles. Vertices are referenced by the coordinates
	 * of the triangle. Edges are referenced by the midpoint of the two
	 * vertices they connect.
	 */
	public static class NegativeSpace extends Coordinate implements Comparable<Coordinate.NegativeSpace> {
		private static final DecimalFormat FMT = new DecimalFormat("0.00");
	
		private static Map<Integer, Coordinate.NegativeSpace> cache = Collections.synchronizedMap(new HashMap<Integer, Coordinate.NegativeSpace>());

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

		public boolean isEdge() {
			return xHundredths == 50 || yHundredths == 25 || yHundredths == 75;
		}

		public List<Coordinate.PositiveSpace> adjacentTiles() {
			List<Coordinate.PositiveSpace> list = new ArrayList<Coordinate.PositiveSpace>(3);
			list.add(Coordinate.PositiveSpace.valueOf(x, (y * 100 + xHundredths - 50) / 100));
			list.add(Coordinate.PositiveSpace.valueOf(x - 1, y - 1));
			list.add(Coordinate.PositiveSpace.valueOf((x * 100 - xHundredths) / 100, y));
			return list;
		}

		public List<Coordinate.NegativeSpace> adjacentEdges() {
			List<Coordinate.NegativeSpace> list;
			if (isEdge()) {
				list = new ArrayList<Coordinate.NegativeSpace>(4);
				//up [yHundredths == 25]/down [yHundredths == 75] left
				list.add(valueOf(x, xHundredths - 50, y, 25));
				//up right [yHundredths == 25]/left [yHundredths == 75]
				list.add(valueOf(x, 2 * xHundredths, y, yHundredths + 50));
				//down [yHundredths == 25]/up [yHundredths == 75] right
				list.add(valueOf(x, xHundredths + 50, y, 2 * yHundredths - 25));
				//down left [yHundredths == 25]/right [yHundredths == 75]
				list.add(valueOf(x, 0, y, yHundredths - 50));
			} else {
				list = new ArrayList<Coordinate.NegativeSpace>(3);
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

		public List<Coordinate.NegativeSpace> adjacentVertices() {
			List<Coordinate.NegativeSpace> list;
			if (isEdge()) {
				list = new ArrayList<Coordinate.NegativeSpace>(2);
				list.add(valueOf(x, (xHundredths - 50) / 100 * 100, y, yHundredths - 25));
				list.add(valueOf(x, (xHundredths + 50) / 100 * 100, y, yHundredths + 25));
			} else {
				list = new ArrayList<Coordinate.NegativeSpace>(3);
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

		public boolean inBounds() {
			if (isEdge())
				return x >= 1 && xHundreds() <= 600 && yHundreds() >= 125 && yHundreds() <= 625
						&& (x > 3 || yHundreds() <= xHundreds() + 275)
						&& (x <= 3 || yHundreds() >= xHundreds() - 225);
			else
				return x >= 1 && x <= 6 && y >= 1 && y <= 6
						&& (x > 3 || yHundreds() <= xHundreds() + 300)
						&& (x <= 3 || yHundreds() > xHundreds() - 300);
		}

		public int[] getVertexCenter(int tileWidth, int tileHeight) {
			if (isEdge()) return null;

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
			if (!isEdge()) return null;

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
		public int compareTo(Coordinate.NegativeSpace other) {
			return this.hashCode() - other.hashCode();
		}

		@Override
		public boolean equals(Object b) {
			if (b instanceof Coordinate.NegativeSpace) {
				Coordinate.NegativeSpace other = (Coordinate.NegativeSpace) b;
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
			return "Coordinate.NegativeSpace[" + FMT.format(x + xHundredths / 100f) +"," + FMT.format(y + yHundredths / 100f) + "]";
		}

		public static Coordinate.NegativeSpace edge(Coordinate.NegativeSpace vertex1, Coordinate.NegativeSpace vertex2) {
			//edge is the midpoint, assuming the vertices are adjacent
			if (vertex1 == null || vertex2 == null || vertex1.isEdge() || vertex2.isEdge() || !vertex1.adjacentVertices().contains(vertex2))
				return null;
			int xHundreds = (vertex1.xHundreds() + vertex2.xHundreds()) / 2;
			int yHundreds = (vertex1.yHundreds() + vertex2.yHundreds()) / 2;
			Coordinate.NegativeSpace edge = valueOf(xHundreds, yHundreds);
			assert edge.isEdge();
			return edge;
		}

		public static Coordinate.NegativeSpace[] vertices(Coordinate.NegativeSpace edge) {
			if (edge == null || !edge.isEdge())
				return null;
			Coordinate.NegativeSpace vertex1, vertex2;
			if (edge.xHundredths == 50) {
				vertex1 = valueOf(edge.xHundreds() - 50, edge.yHundreds() - 25);
				vertex2 = valueOf(edge.xHundreds() + 50, edge.yHundreds() + 25);
			} else {
				vertex1 = valueOf(edge.xHundreds(), edge.yHundreds() - 25);
				vertex2 = valueOf(edge.xHundreds(), edge.yHundreds() + 25);
			}
			assert !vertex1.isEdge() && !vertex2.isEdge();
			return new Coordinate.NegativeSpace[] { vertex1, vertex2 };
		}

		public static Coordinate.NegativeSpace valueOf(int x, int xHundredths, int y, int yHundredths) {
			byte[] xNorm = normalize(x, xHundredths);
			byte[] yNorm = normalize(y, yHundredths);
			Integer key = Integer.valueOf(hashCode(xNorm[0], xNorm[1], yNorm[0], yNorm[1]));
			Coordinate.NegativeSpace value = cache.get(key);
			if (value == null) {
				value = new Coordinate.NegativeSpace(xNorm, yNorm);
				cache.put(key, value);
			}
			return value;
		}

		public static Coordinate.NegativeSpace valueOf(int xHundreds, int yHundreds) {
			return valueOf(xHundreds / 100, xHundreds % 100, yHundreds / 100, yHundreds % 100);
		}
	}
}
