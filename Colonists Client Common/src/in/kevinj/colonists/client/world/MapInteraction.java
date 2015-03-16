package in.kevinj.colonists.client.world;

import in.kevinj.colonists.client.ContinuousRendererUtil;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Vector3;

//hexagons are interesting to work with
//TODO: detect dragging, implement pinch/scrollwheel zooming
public class MapInteraction {
	private final WorldModel model;

	public boolean hidden;

	private boolean down;
	private Coordinate.PositiveSpace tileTarget;
	private Coordinate.NegativeSpace vertexTarget, edgeTarget;

	public MapInteraction(WorldModel model) {
		this.model = model;
	}

	private Coordinate.PositiveSpace getTile(Vector3 cursor) {
		if (cursor == null) return null;

		double sideTriangleWidth = model.tileWidth / 4d;
		double rightTriangleOffset = model.tileWidth - sideTriangleWidth; // == model.tileWidth / 4 * 3
		double trapezoidHeight = model.tileHeight / 2d;

		int x = (int) (cursor.x / rightTriangleOffset);
		double cursorY = cursor.y - (model.tileHeight - trapezoidHeight * x);
		if (cursor.x < 0 || cursorY < 0)
			return null;

		int y = (int) (cursorY / model.tileHeight);

		double withinX = cursor.x - x * rightTriangleOffset;
		double withinY = cursorY - y * model.tileHeight;
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
		if (x < 3) {
			if (y > x + 3)
				return null;
		} else {
			if (y < x - 3)
				return null;
		}

		return Coordinate.PositiveSpace.valueOf(x, y);
	}

	private Coordinate.NegativeSpace getVertex(Vector3 cursor, Coordinate.PositiveSpace tile) {
		if (cursor == null || tile == null) return null;

		double sideTriangleWidth = model.tileWidth / 4d;
		double rightTriangleOffset = model.tileWidth - sideTriangleWidth; // == model.tileWidth / 4 * 3
		double trapezoidHeight = model.tileHeight / 2d;
		double halfWidth = model.tileWidth / 2d;

		int x = tile.x;
		int y = tile.y;
		int xHundredths = 0;
		int yHundredths = 0;
		double withinX = cursor.x - x * rightTriangleOffset;
		double cursorY = cursor.y - (model.tileHeight - trapezoidHeight * x);
		double withinY = cursorY - y * model.tileHeight;
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
		return Coordinate.NegativeSpace.valueOf(x, xHundredths, y, yHundredths);
	}

	private Coordinate.NegativeSpace getEdge(Vector3 cursor, Coordinate.PositiveSpace tile, Coordinate.NegativeSpace vertex) {
		if (cursor == null || tile == null || vertex == null) return null;

		double sideTriangleWidth = model.tileWidth / 4d;
		double rightTriangleOffset = model.tileWidth - sideTriangleWidth; // == model.tileWidth / 4 * 3
		double trapezoidHeight = model.tileHeight / 2d;
		double halfWidth = model.tileWidth / 2d;

		Coordinate.NegativeSpace otherVertex = null;
		double withinX = cursor.x - tile.x * rightTriangleOffset;
		double cursorY = cursor.y - (model.tileHeight - trapezoidHeight * tile.x);
		double withinY = cursorY - tile.y * model.tileHeight;
		if (tile.x == vertex.x) {
			if (vertex.y != tile.y) //left up
				if (withinY > trapezoidHeight - trapezoidHeight / (halfWidth - sideTriangleWidth) * (withinX - halfWidth))
					otherVertex = Coordinate.NegativeSpace.valueOf(vertex.x + 1, vertex.xHundredths, vertex.y, vertex.yHundredths + 50);
				else
					otherVertex = Coordinate.NegativeSpace.valueOf(vertex.x, vertex.xHundredths, vertex.y, vertex.yHundredths - 50);
			else if (vertex.yHundredths != 0) //left middle
				if (withinY > trapezoidHeight)
					otherVertex = Coordinate.NegativeSpace.valueOf(vertex.x, vertex.xHundredths, vertex.y, vertex.yHundredths + 50);
				else
					otherVertex = Coordinate.NegativeSpace.valueOf(vertex.x, vertex.xHundredths, vertex.y, vertex.yHundredths - 50);
			else //left down
				if (withinY > trapezoidHeight + trapezoidHeight / (halfWidth - sideTriangleWidth) * (withinX - halfWidth))
					otherVertex = Coordinate.NegativeSpace.valueOf(vertex.x, vertex.xHundredths, vertex.y, vertex.yHundredths + 50);
				else
					otherVertex = Coordinate.NegativeSpace.valueOf(vertex.x + 1, vertex.xHundredths, vertex.y, vertex.yHundredths + 50);
		} else {
			if (vertex.y != tile.y + 1) //right down
				if (withinY > trapezoidHeight - trapezoidHeight / (halfWidth - sideTriangleWidth) * (withinX - halfWidth))
					otherVertex = Coordinate.NegativeSpace.valueOf(vertex.x, vertex.xHundredths, vertex.y, vertex.yHundredths + 50);
				else
					otherVertex = Coordinate.NegativeSpace.valueOf(vertex.x - 1, vertex.xHundredths, vertex.y, vertex.yHundredths - 50);
			else if (vertex.yHundredths != 50) //right middle
				if (withinY > trapezoidHeight)
					otherVertex = Coordinate.NegativeSpace.valueOf(vertex.x, vertex.xHundredths, vertex.y, vertex.yHundredths + 50);
				else
					otherVertex = Coordinate.NegativeSpace.valueOf(vertex.x, vertex.xHundredths, vertex.y, vertex.yHundredths - 50);
			else //right up
				if (withinY > trapezoidHeight + trapezoidHeight / (halfWidth - sideTriangleWidth) * (withinX - halfWidth))
					otherVertex = Coordinate.NegativeSpace.valueOf(vertex.x - 1, vertex.xHundredths, vertex.y, vertex.yHundredths - 50);
				else
					otherVertex = Coordinate.NegativeSpace.valueOf(vertex.x, vertex.xHundredths, vertex.y, vertex.yHundredths - 50);
		}

		//get the midpoint
		int xHundreds = (vertex.x * 100 + vertex.xHundredths + otherVertex.x * 100 + otherVertex.xHundredths) / 2;
		int yHundreds = (vertex.y * 100 + vertex.yHundredths + otherVertex.y * 100 + otherVertex.yHundredths) / 2;
		return Coordinate.NegativeSpace.valueOf(xHundreds / 100, xHundreds % 100, yHundreds / 100, yHundreds % 100);
	}

	private void unsetTargets() {
		tileTarget = null;
		vertexTarget = edgeTarget = null;
	}

	private void sendMove(PlayerAction move) {
		for (int i = 0; i < 4; i++)
			model.getPlayer(i).sendMove(move);
	}

	public void update(float tDelta) {
		if (hidden) {
			unsetTargets();
			return;
		}

		if (!model.getPlayer(model.getCurrentPlayerTurn()).isPlayable())
			return;

		Vector3 cursor = model.parent.controller.getCursor(model);
		boolean wasDown = down;
		down = Gdx.input.isButtonPressed(Buttons.LEFT);
		if (!down) {
			if (!wasDown) {
				unsetTargets();
			} else {
				//make sure Android doesn't show old frames when animating to home/recents screen
				ContinuousRendererUtil.instance.doShortContinuousRender();

				if (tileTarget != null) {
					sendMove(new PlayerAction.EndConsiderMove(model, Coordinate.CoordinateType.TILE));
					sendMove(new PlayerAction.CommitMove(model, Coordinate.CoordinateType.TILE, tileTarget));
				}
				if (vertexTarget != null) {
					sendMove(new PlayerAction.EndConsiderMove(model, Coordinate.CoordinateType.VERTEX));
					sendMove(new PlayerAction.CommitMove(model, Coordinate.CoordinateType.VERTEX, vertexTarget));
				}
				if (edgeTarget != null) {
					sendMove(new PlayerAction.EndConsiderMove(model, Coordinate.CoordinateType.EDGE));
					sendMove(new PlayerAction.CommitMove(model, Coordinate.CoordinateType.EDGE, edgeTarget));
				}
			}
		} else {
			Coordinate.PositiveSpace oldTileTarget = tileTarget;
			Coordinate.NegativeSpace oldVertexTarget = vertexTarget, oldEdgeTarget = edgeTarget;

			//get tile coordinates for highwayman
			tileTarget = getTile(cursor);
			//get triangle coordinates for villages, metros
			vertexTarget = getVertex(cursor, tileTarget);
			//get midpoint coordinates for roads
			edgeTarget = getEdge(cursor, tileTarget, vertexTarget);

			//disambiguate selection. if cursor is close enough to vertex,
			//then select vertex. otherwise, select if cursor is close to edge,
			//then select edge. otherwise, select tile.
			if (vertexTarget != null) {
				//get the distance from between point (settlement) and point (cursor)
				int[] center = vertexTarget.getVertexCenter(model.tileWidth, model.tileHeight);
				double a = cursor.x - center[0];
				double b = cursor.y - center[1];
				double dis = Math.sqrt(a * a + b * b);
				if (dis > model.settlementRadius) {
					vertexTarget = null;
				} else {
					tileTarget = null;
					edgeTarget = null;
				}
			}
			if (edgeTarget != null) {
				//get the perpendicular distance between line (road) and point (cursor), given line in point slope form
				int[] info = edgeTarget.getEdgeXYR(model.tileWidth, model.tileHeight);
				double slope = Math.tan(info[2] * Math.PI / 180);
				//y - y_1 = m * (x - x_1) <=> m * x + -1 * y + (y_1 - m * x_1) = 0
				double a = slope;
				double b = -1;
				double c = info[1] - slope * info[0];
				//a * a > 0 and b * b = 1, so no possibility of / by 0
				double dis = Math.abs(a * cursor.x + b * cursor.y + c) / Math.sqrt(a * a + b * b);
				if (dis > 32)
					edgeTarget = null;
				else
					tileTarget = null;
			}

			if (oldTileTarget != null && !oldTileTarget.equals(tileTarget))
				sendMove(new PlayerAction.EndConsiderMove(model, Coordinate.CoordinateType.TILE));
			if (oldVertexTarget != null && !oldVertexTarget.equals(vertexTarget))
				sendMove(new PlayerAction.EndConsiderMove(model, Coordinate.CoordinateType.VERTEX));
			if (oldEdgeTarget != null && !oldEdgeTarget.equals(edgeTarget))
				sendMove(new PlayerAction.EndConsiderMove(model, Coordinate.CoordinateType.EDGE));
			if (tileTarget != null && !tileTarget.equals(oldTileTarget))
				sendMove(new PlayerAction.BeginConsiderMove(model, Coordinate.CoordinateType.TILE, tileTarget));
			if (vertexTarget != null && !vertexTarget.equals(oldVertexTarget))
				sendMove(new PlayerAction.BeginConsiderMove(model, Coordinate.CoordinateType.VERTEX, vertexTarget));
			if (edgeTarget != null && !edgeTarget.equals(oldEdgeTarget))
				sendMove(new PlayerAction.BeginConsiderMove(model, Coordinate.CoordinateType.EDGE, edgeTarget));
		}
	}
}
