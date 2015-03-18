package in.kevinj.colonists.world;

import in.kevinj.colonists.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
			return GraphUtil.searchForAvailable(currentPlayerSettlements, currentPlayerRoads);
		}

		private static boolean isConsistent(GameMap<?> map) {
			List<Set<Coordinate.NegativeSpace>> correctMoves = availableMovesCleanUpdate(map);
			for (int i = 0; i < 4; i++)
				if (!map.getPlayer(i).availableMoves.equals(correctMoves.get(i)))
					return false;
			return true;
		}

		public static void incrementalUpdateAfterAddToGrid(GameMap<?> map, Coordinate.NegativeSpace loc, Entity.NegativeSpace added) {
			Map<Coordinate.NegativeSpace, ? extends Entity.NegativeSpace> grid = map.getGrid();
			Entity.NegativeSpace tmp;
			for (int i = 0; i < 4; i++)
				map.getPlayer(i).availableMoves.remove(loc);
			boolean isOrphaned = true, isSettlementError = false;
			if (loc.isEdge()) {
				for (Coordinate.NegativeSpace neighbor : loc.adjacentEdges())
					if (grid.containsKey(neighbor))
						isOrphaned = false;
					else if (neighbor.inBounds())
						//can't build a road through another user's settlement
						if ((tmp = grid.get(Coordinate.NegativeSpace.intersection(loc, neighbor))) == null || tmp.getPlayer() == added.getPlayer())
							map.getPlayer(added.getPlayer()).availableMoves.add(neighbor);
				for (Coordinate.NegativeSpace neighbor : loc.adjacentVertices()) {
					if (grid.containsKey(neighbor)) {
						isOrphaned = false;
					} else if (neighbor.inBounds()) {
						boolean good = true;
						for (Coordinate.NegativeSpace neighborSquared : neighbor.adjacentVertices()) {
							if (grid.containsKey(neighborSquared)) {
								good = false;
								break;
							}
						}
						if (good)
							map.getPlayer(added.getPlayer()).availableMoves.add(neighbor);
					}
				}
			} else {
				for (Coordinate.NegativeSpace edge : loc.adjacentEdges())
					if (grid.containsKey(edge))
						isOrphaned = false;
					else if (edge.inBounds())
						map.getPlayer(added.getPlayer()).availableMoves.add(edge);
				for (Coordinate.NegativeSpace neighbor : loc.adjacentVertices())
					if (grid.containsKey(neighbor))
						isSettlementError = true;
					else for (int i = 0; i < 4; i++)
						map.getPlayer(i).availableMoves.remove(neighbor);
			}
			if (isOrphaned || isSettlementError)
				//graph is invalid
				for (int i = 0; i < 4; i++)
					map.getPlayer(i).availableMoves.clear();
			assert isConsistent(map);
		}

		public static void incrementalUpdateAfterRemoveFromGrid(GameMap<?> map, Coordinate.NegativeSpace loc, Entity.NegativeSpace removed) {
			Map<Coordinate.NegativeSpace, ? extends Entity.NegativeSpace> grid = map.getGrid();
			Entity.NegativeSpace tmp, neighborEnt;
			//could use a BitSet too since integral values are in [0, 4]
			Set<Integer> connectedTo = new HashSet<Integer>();
			if (loc.isEdge()) {
				//deleted an edge. invalidate any moves to orphaned unoccupied
				//"vertices"/"edges" and clear all moves if any occupied
				//"vertices"/"edges" are orphaned. add opened up move to edge.
				boolean isCutEdge, isOrphaned = false;
				for (Iterator<Coordinate.NegativeSpace> iter = loc.adjacentEdges().iterator(); !isOrphaned && iter.hasNext(); ) {
					Coordinate.NegativeSpace neighbor = iter.next();
					neighborEnt = grid.get(neighbor);
					isCutEdge = true;
					//check if neighboring edge is connected to our other edges
					for (Coordinate.NegativeSpace otherEdge : neighbor.adjacentEdges()) {
						if ((tmp = grid.get(otherEdge)) != null && tmp.getPlayer() == removed.getPlayer()) {
							isCutEdge = false;
							break;
						}
					}
					//check if neighboring edge is connected to one of our vertices 
					for (Coordinate.NegativeSpace otherVertex : neighbor.adjacentVertices()) {
						if ((tmp = grid.get(otherVertex)) != null && tmp.getPlayer() == removed.getPlayer()) {
							isCutEdge = false;
							break;
						}
					}
					//if neighboring edge is not connected, then we have an orphaned edge
					if (neighborEnt != null) {
						//can't build a road through another user's settlement
						if ((tmp = grid.get(Coordinate.NegativeSpace.intersection(neighbor, loc))) == null || neighborEnt.getPlayer() == tmp.getPlayer())
							connectedTo.add(neighborEnt.getPlayer());
						if (isCutEdge)
							//road is not connected to any of our vertices or other edges:
							//we have an orphaned road
							isOrphaned = true;
					} else {
						if (isCutEdge)
							//empty edge is not connected to any of our vertices or other edges:
							//there is no longer any way to reach the edge
							map.getPlayer(removed.getPlayer()).availableMoves.remove(neighbor);
					}
				}
				for (Iterator<Coordinate.NegativeSpace> iter = loc.adjacentVertices().iterator(); !isOrphaned && iter.hasNext(); ) {
					Coordinate.NegativeSpace neighbor = iter.next();
					neighborEnt = grid.get(neighbor);
					isCutEdge = true;
					//check if neighboring vertex is connected to our other edges
					for (Coordinate.NegativeSpace otherEdge : neighbor.adjacentEdges()) {
						if ((tmp = grid.get(otherEdge)) != null && tmp.getPlayer() == removed.getPlayer()) {
							isCutEdge = false;
							break;
						}
					}
					//if neighboring vertex is not connected, then we have an orphaned edge
					if (neighborEnt != null) {
						connectedTo.add(neighborEnt.getPlayer());
						if (isCutEdge)
							//settlement is not connected to any of our edges:
							//we have an orphaned settlement
							isOrphaned = true;
					} else {
						if (isCutEdge)
							//empty vertex is not connected to any of our edges:
							//there is no longer any way to reach the vertex
							map.getPlayer(removed.getPlayer()).availableMoves.remove(neighbor);
					}
				}
				if (isOrphaned)
					//graph is invalid
					for (int i = 0; i < 4; i++)
						map.getPlayer(i).availableMoves.clear();
				else for (Integer player : connectedTo)
					//the removed edge can be reached from player's own "edges"/"vertices"
					map.getPlayer(player.intValue()).availableMoves.add(loc);
			} else {
				//deleted a vertex. clear all moves if any occupied "edges" are
				//orphaned. add opened up moves to vertices
				boolean isCutVertex, isOrphaned = false;
				for (Iterator<Coordinate.NegativeSpace> iter = loc.adjacentEdges().iterator(); !isOrphaned && iter.hasNext(); ) {
					Coordinate.NegativeSpace neighbor = iter.next();
					neighborEnt = grid.get(neighbor);
					isCutVertex = true;
					//check if neighboring edge is connected to our other edges
					for (Coordinate.NegativeSpace otherEdge : neighbor.adjacentEdges()) {
						if ((tmp = grid.get(otherEdge)) != null) {
							if (tmp.getPlayer() == removed.getPlayer())
								isCutVertex = false;
							else if (neighborEnt == null)
								//this settlement may be blocking some other
								//player from extending his road
								map.getPlayer(tmp.getPlayer()).availableMoves.add(neighbor);
						}
					}
					//check if neighboring edge is connected to one of our vertices 
					for (Coordinate.NegativeSpace otherVertex : neighbor.adjacentVertices()) {
						if ((tmp = grid.get(otherVertex)) != null && tmp.getPlayer() == removed.getPlayer()) {
							isCutVertex = false;
							break;
						}
					}
					//if neighboring edge is not connected, then we have an orphaned edge
					if (neighborEnt != null) {
						connectedTo.add(neighborEnt.getPlayer());
						if (isCutVertex)
							isOrphaned = true;
					}
					//if vertex is removed, neighboring edge must either be orphaned
					//or connected to another of our edges, so availbleMoves not changed
				}
				if (isOrphaned)
					//graph is invalid
					for (int i = 0; i < 4; i++)
						map.getPlayer(i).availableMoves.clear();
				else for (Integer player : connectedTo)
					//the removed vertex can be reached from player's own "edges"
					map.getPlayer(player.intValue()).availableMoves.add(loc);
				//elimination of one vertex may open up adjacent vertices
				for (Iterator<Coordinate.NegativeSpace> iter = loc.adjacentVertices().iterator(); !isOrphaned && iter.hasNext(); ) {
					Coordinate.NegativeSpace neighbor = iter.next();
					connectedTo = new HashSet<Integer>();
					//get set of players with edges neighboring the neighboring vertex
					for (Coordinate.NegativeSpace otherEdge : neighbor.adjacentEdges()) {
						if ((tmp = grid.get(otherEdge)) != null) {
							connectedTo.add(tmp.getPlayer());
							break;
						}
					}
					//if neighboring vertex is adjacent to another vertex, move is invalid
					for (Coordinate.NegativeSpace otherVertex : neighbor.adjacentVertices()) {
						if (grid.containsKey(otherVertex)) {
							connectedTo.clear();
							break;
						}
					}
					for (Integer player : connectedTo)
						map.getPlayer(player.intValue()).availableMoves.add(neighbor);
				}
			}
			assert isConsistent(map);
		}
	}
}
