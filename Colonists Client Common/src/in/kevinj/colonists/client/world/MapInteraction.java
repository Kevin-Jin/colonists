package in.kevinj.colonists.client.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Vector3;

//hexagons are interesting to work with
//TODO: detect dragging, implement pinch/scrollwheel zooming
public class MapInteraction {
	private final WorldModel model;

	public boolean hidden;
	public int tileWidth, tileHeight;

	private boolean down;
	private WorldModel.TileCoordinate tileTarget;
	private WorldModel.EntityCoordinate vertexTarget, edgeTarget;

	public MapInteraction(WorldModel model) {
		this.model = model;
	}

	private WorldModel.TileCoordinate getTile(Vector3 cursor) {
		if (cursor == null) return null;

		double sideTriangleWidth = tileWidth / 4d;
		double rightTriangleOffset = tileWidth - sideTriangleWidth; // == tileWidth / 4 * 3
		double trapezoidHeight = tileHeight / 2d;

		int x = (int) (cursor.x / rightTriangleOffset);
		double cursorY = cursor.y - (tileHeight - trapezoidHeight * x);
		if (cursor.x < 0 || cursorY < 0)
			return null;

		int y = (int) (cursorY / tileHeight);

		double withinX = cursor.x - x * rightTriangleOffset;
		double withinY = cursorY - y * tileHeight;
		//areas that are ambiguous under the rectangular heuristic
		if (withinX < sideTriangleWidth) {
			if (withinY < trapezoidHeight) {
				//bottom triangle. check if withinY is below the line with slope
				//(-trapezoidHeight / sideTriangleWidth) and y-intercept trapezoidHeight
				if (withinY < trapezoidHeight - trapezoidHeight / sideTriangleWidth * withinX) {
					x--;
					y--;
				}
			} else {
				//top triangle. check if withinY is above the line with slope
				//(trapezoidHeight / sideTriangleWidth) and y-intercept trapezoidHeight
				if (withinY > trapezoidHeight + trapezoidHeight / sideTriangleWidth * withinX) {
					x--;
				}
			}
		}
		if (x < 0 || x >= model.mapBoundsColumns || y < 0 || y >= model.mapBoundsRows)
			return null;

		return new WorldModel.TileCoordinate(x, y);
	}

	private WorldModel.EntityCoordinate getVertex(Vector3 cursor, WorldModel.TileCoordinate tile) {
		if (cursor == null || tile == null) return null;

		double sideTriangleWidth = tileWidth / 4d;
		double rightTriangleOffset = tileWidth - sideTriangleWidth; // == tileWidth / 4 * 3
		double trapezoidHeight = tileHeight / 2d;
		double halfWidth = tileWidth / 2d;

		int x = tile.x;
		int y = tile.y;
		int xHundredths = 0;
		int yHundredths = 0;
		double withinX = cursor.x - x * rightTriangleOffset;
		double cursorY = cursor.y - (tileHeight - trapezoidHeight * x);
		double withinY = cursorY - y * tileHeight;
		if (withinX > halfWidth) {
			x++;
			if (withinY > trapezoidHeight + trapezoidHeight / rightTriangleOffset * (withinX - halfWidth))
				yHundredths += 150;
			else if (withinY > trapezoidHeight - trapezoidHeight / rightTriangleOffset * (withinX - halfWidth))
				yHundredths += 100;
			else
				yHundredths += 50;
		} else {
			if (withinY > trapezoidHeight - trapezoidHeight / rightTriangleOffset * (withinX - halfWidth))
				yHundredths += 100;
			else if (withinY > trapezoidHeight + trapezoidHeight / rightTriangleOffset * (withinX - halfWidth))
				yHundredths += 50;
		}
		return new WorldModel.EntityCoordinate(x, xHundredths, y, yHundredths);
	}

	private WorldModel.EntityCoordinate getEdge(Vector3 cursor, WorldModel.TileCoordinate tile, WorldModel.EntityCoordinate vertex) {
		if (cursor == null || tile == null || vertex == null) return null;

		double sideTriangleWidth = tileWidth / 4d;
		double rightTriangleOffset = tileWidth - sideTriangleWidth; // == tileWidth / 4 * 3
		double trapezoidHeight = tileHeight / 2d;
		double halfWidth = tileWidth / 2d;

		WorldModel.EntityCoordinate otherVertex = null;
		double withinX = cursor.x - tile.x * rightTriangleOffset;
		double cursorY = cursor.y - (tileHeight - trapezoidHeight * tile.x);
		double withinY = cursorY - tile.y * tileHeight;
		if (tile.x == vertex.x) {
			if (vertex.y != tile.y) //left up
				if (withinY > trapezoidHeight - trapezoidHeight / (halfWidth - sideTriangleWidth) * (withinX - halfWidth))
					otherVertex = new WorldModel.EntityCoordinate(vertex.x + 1, vertex.xHundredths, vertex.y, vertex.yHundredths + 50);
				else
					otherVertex = new WorldModel.EntityCoordinate(vertex.x, vertex.xHundredths, vertex.y, vertex.yHundredths - 50);
			else if (vertex.yHundredths != 0) //left middle
				if (withinY > trapezoidHeight)
					otherVertex = new WorldModel.EntityCoordinate(vertex.x, vertex.xHundredths, vertex.y, vertex.yHundredths + 50);
				else
					otherVertex = new WorldModel.EntityCoordinate(vertex.x, vertex.xHundredths, vertex.y, vertex.yHundredths - 50);
			else //left down
				if (withinY > trapezoidHeight + trapezoidHeight / (halfWidth - sideTriangleWidth) * (withinX - halfWidth))
					otherVertex = new WorldModel.EntityCoordinate(vertex.x, vertex.xHundredths, vertex.y, vertex.yHundredths + 50);
				else
					otherVertex = new WorldModel.EntityCoordinate(vertex.x + 1, vertex.xHundredths, vertex.y, vertex.yHundredths + 50);
		} else {
			if (vertex.y != tile.y + 1) //right down
				if (withinY > trapezoidHeight - trapezoidHeight / (halfWidth - sideTriangleWidth) * (withinX - halfWidth))
					otherVertex = new WorldModel.EntityCoordinate(vertex.x, vertex.xHundredths, vertex.y, vertex.yHundredths + 50);
				else
					otherVertex = new WorldModel.EntityCoordinate(vertex.x - 1, vertex.xHundredths, vertex.y, vertex.yHundredths - 50);
			else if (vertex.yHundredths != 50) //right middle
				if (withinY > trapezoidHeight)
					otherVertex = new WorldModel.EntityCoordinate(vertex.x, vertex.xHundredths, vertex.y, vertex.yHundredths + 50);
				else
					otherVertex = new WorldModel.EntityCoordinate(vertex.x, vertex.xHundredths, vertex.y, vertex.yHundredths - 50);
			else //right up
				if (withinY > trapezoidHeight + trapezoidHeight / (halfWidth - sideTriangleWidth) * (withinX - halfWidth))
					otherVertex = new WorldModel.EntityCoordinate(vertex.x - 1, vertex.xHundredths, vertex.y, vertex.yHundredths - 50);
				else
					otherVertex = new WorldModel.EntityCoordinate(vertex.x, vertex.xHundredths, vertex.y, vertex.yHundredths - 50);
		}

		//get the midpoint
		int xHundreds = (vertex.x * 100 + vertex.xHundredths + otherVertex.x * 100 + otherVertex.xHundredths) / 2;
		int yHundreds = (vertex.y * 100 + vertex.yHundredths + otherVertex.y * 100 + otherVertex.yHundredths) / 2;
		return new WorldModel.EntityCoordinate(xHundreds / 100, xHundreds % 100, yHundreds / 100, yHundreds % 100);
	}

	public WorldModel.TileCoordinate getSelectedTile() {
		if (down)
			return null;

		return tileTarget;
	}

	public WorldModel.EntityCoordinate getSelectedVertex() {
		if (down)
			return null;

		return vertexTarget;
	}

	public WorldModel.EntityCoordinate getSelectedEdge() {
		if (down)
			return null;

		return edgeTarget;
	}

	private void unsetTargets() {
		tileTarget = null;
		vertexTarget = edgeTarget = null;
	}

	public void update(float tDelta) {
		if (hidden) {
			unsetTargets();
			return;
		}

		Vector3 cursor = model.parent.controller.getCursor(model);
		boolean wasDown = down;
		down = Gdx.input.isButtonPressed(Buttons.LEFT);
		if (!down) {
			if (wasDown) {
				//check if our pointer moved off the location we started at
				if (tileTarget != null && !tileTarget.equals(getTile(cursor)))
					tileTarget = null;

				if (vertexTarget != null && !vertexTarget.equals(getVertex(cursor, tileTarget)))
					vertexTarget = null;

				if (edgeTarget != null && !edgeTarget.equals(getEdge(cursor, tileTarget, vertexTarget)))
					edgeTarget = null;
			} else {
				unsetTargets();
			}
		} else {
			if (!wasDown) {
				//get tile coordinates for highwayman
				tileTarget = getTile(cursor);
				//get triangle coordinates for villages, metros
				vertexTarget = getVertex(cursor, tileTarget);
				//get midpoint coordinates for roads
				edgeTarget = getEdge(cursor, tileTarget, vertexTarget);
			}
		}
	}
}
