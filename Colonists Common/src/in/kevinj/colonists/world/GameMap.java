package in.kevinj.colonists.world;

import in.kevinj.colonists.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public interface GameMap<T extends Entity.NegativeSpace> {
	public Map<Coordinate.NegativeSpace, T> getGrid();
	public T addToGrid(Coordinate.NegativeSpace loc, T ent);
	public Entity removeFromGrid(Coordinate.NegativeSpace loc);
	public int getCurrentPlayerTurn();
	public Player getPlayer(int id);
	public Entity.PositiveSpace getHighwayman();
	public void setHighwaymanCandidate(Coordinate.PositiveSpace coord);
	public void setRoadCandidate(Coordinate.NegativeSpace coord);
	public void setMetroCandidate(Coordinate.NegativeSpace coord);
	public void setVillageCandidate(Coordinate.NegativeSpace coord);

	public static class Helper {
		public static void initializeMap(MapTile[][] resources, Entity.PositiveSpace highwayman) {
			Random r = new Random();

			//keystone water tiles only touch a resource tile on one edge (i.e.
			//corner in the hexagon that the map makes). these keystones exist at
			//(0, 0), (0, 3), (3, 0), (3, 6), (6, 3), (6, 6). pick a
			//random one as a reference point to make PortTile.getRandomPorts()
			//easier to implement.
			int keystone = r.nextInt(6);
			int x, y, rot;
			switch (keystone) {
				case 0:	x = 3;	y = 0;	rot = 0;	break;
				case 1:	x = 6;	y = 3;	rot = 60;	break;
				case 2:	x = 6;	y = 6;	rot = 120;	break;
				case 3:	x = 3;	y = 6;	rot = 180;	break;
				case 4:	x = 0;	y = 3;	rot = 240;	break;
				case 5:	x = 0;	y = 0;	rot = 300;	break;
				default:	throw new AssertionError("Wrong keystone ID");
			}
			Queue<MapTile> tiles = new LinkedList<MapTile>();
			MapTile.PortTile.getRandomPorts(r, rot, tiles);
			MapTile.ResourceTile.getRandomResources(r, tiles);
			for (int rad = 3; rad >= 0; --rad) {
				for (int i = 0; i < Math.max(1, 6 * rad); i++) {
					resources[y][x] = tiles.poll();
					if (resources[y][x].isResource() && resources[y][x].getResourceType() == MapTile.ResourceType.WASTELAND)
						highwayman.setPosition(Coordinate.PositiveSpace.valueOf(x, y));
					if (y == 3 - rad) {
						if (x == 3) y++;
						x++;
					} else if (y == 3 + rad) {
						if (x == 3) y--;
						x--;
					} else if (x > 3) {
						if (x != 3 + rad) x++;
						y++;
					} else if (x < 3) {
						if (x != 3 - rad) x--;
						y--;
					}
				}
				if (y == 3 - rad) {
					if (x != 3) x++;
					y++;
				} else if (y == 3 + rad) {
					if (x != 3) x--;
					y--;
				} else if (x == 3 - rad) {
					x++;
				} else if (x == 3 + rad) {
					x--;
				}
			}
		}

		public static List<Set<Coordinate.NegativeSpace>> availableMovesCleanUpdate(GameMap<?> map) {
			Map<Coordinate.NegativeSpace, Entity.NegativeSpace> currentPlayerSettlements = new HashMap<Coordinate.NegativeSpace, Entity.NegativeSpace>();
			Map<Coordinate.NegativeSpace, Entity.NegativeSpace> currentPlayerRoads = new HashMap<Coordinate.NegativeSpace, Entity.NegativeSpace>();
			for (Map.Entry<Coordinate.NegativeSpace, ? extends Entity.NegativeSpace> entry: map.getGrid().entrySet()) {
				if (entry.getKey().isEdge())
					currentPlayerRoads.put(entry.getKey(), entry.getValue());
				else
					currentPlayerSettlements.put(entry.getKey(), entry.getValue());
			}
			currentPlayerSettlements = Collections.unmodifiableMap(currentPlayerSettlements);
			currentPlayerRoads = Collections.unmodifiableMap(currentPlayerRoads);
			return GraphUtil.dfsForAvailable(currentPlayerSettlements, currentPlayerRoads);
		}

		public static void incrementUpdateAfterAddToGrid(GameMap<?> map, Coordinate.NegativeSpace loc, Entity.NegativeSpace ent) {
			//incremental update available moves
			//TODO: update availableMoves for all players
			Map<Coordinate.NegativeSpace, ? extends Entity.NegativeSpace> grid = map.getGrid();
			map.getPlayer(map.getCurrentPlayerTurn()).availableMoves.remove(loc);
			if (loc.isEdge()) {
				Coordinate.NegativeSpace[] vertices = Coordinate.NegativeSpace.vertices(loc);
				for (Coordinate.NegativeSpace neighbor : vertices)
					if (!grid.containsKey(neighbor) && neighbor.inBounds())
						map.getPlayer(map.getCurrentPlayerTurn()).availableMoves.add(neighbor);
			} else {
				for (Coordinate.NegativeSpace edge : loc.adjacentEdges())
					if (!grid.containsKey(edge) && edge.inBounds())
						map.getPlayer(map.getCurrentPlayerTurn()).availableMoves.add(edge);
			}
			assert map.getPlayer(map.getCurrentPlayerTurn()).availableMoves.equals(availableMovesCleanUpdate(map).get(map.getCurrentPlayerTurn()));
		}

		public static void incrementUpdateAfterRemoveFromGrid(GameMap<?> map, Coordinate.NegativeSpace loc) {
			//incremental update available moves
			//TODO: update availableMoves for all players
			Map<Coordinate.NegativeSpace, ? extends Entity.NegativeSpace> grid = map.getGrid();
			if (loc.isEdge()) {
				boolean connected = false, bridge;
				for (Coordinate.NegativeSpace neighbor : Coordinate.NegativeSpace.vertices(loc)) {
					if (grid.containsKey(neighbor)) {
						connected = true; //TODO: connected = (grid.get(neighbor) is our own settlement)
						continue;
					}

					bridge = true;
					for (Coordinate.NegativeSpace otherEdge : neighbor.adjacentEdges()) {
						if (grid.containsKey(otherEdge)) { //TODO: if (grid.containsKey(otherEdge) && grid.get(otherEdge) is our own road)
							bridge = false;
							break;
						}
					}

					//if the empty vertex is not connected to any of our other edges,
					//there is no longer any way to reach the vertex
					if (bridge)
						map.getPlayer(map.getCurrentPlayerTurn()).availableMoves.remove(neighbor);
				}
				if (connected)
					map.getPlayer(map.getCurrentPlayerTurn()).availableMoves.add(loc);
			} else {
				boolean connected = false, articulationPoint;
				for (Coordinate.NegativeSpace edge : loc.adjacentEdges()) {
					if (grid.containsKey(edge)) {
						connected = true; //TODO: connected = (grid.get(edge) is our own road)
						continue;
					}

					articulationPoint = true;
					for (Coordinate.NegativeSpace otherVertex : edge.adjacentVertices()) {
						if (grid.containsKey(otherVertex)) { //TODO: if (grid.containsKey(otherVertex) && grid.get(otherVertex) is our own settlement)
							articulationPoint = false;
							break;
						}
					}

					//if the empty edge is not connected to any of our other vertices,
					//there is no longer any way to reach the edge
					if (articulationPoint)
						map.getPlayer(map.getCurrentPlayerTurn()).availableMoves.remove(edge);
				}
				if (connected)
					map.getPlayer(map.getCurrentPlayerTurn()).availableMoves.add(loc);
			}
			assert map.getPlayer(map.getCurrentPlayerTurn()).availableMoves.equals(GameMap.Helper.availableMovesCleanUpdate(map).get(map.getCurrentPlayerTurn()));
		}
	}
}
