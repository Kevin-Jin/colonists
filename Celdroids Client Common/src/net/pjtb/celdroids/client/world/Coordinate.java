package net.pjtb.celdroids.client.world;

public class Coordinate {
	public int col;
	public int row;

	public Coordinate(int col, int row) {
		this.col = col;
		this.row = row;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Coordinate))
			return false;

		Coordinate other = (Coordinate) obj;
		return (other.col == this.col && other.row == this.row);
	}

	@Override
	public int hashCode() {
		return col << 16 | row;
	}

	@Override
	public String toString() {
		return "Coordinate[col:" + col + ",row:" + row + "]";
	}
}
