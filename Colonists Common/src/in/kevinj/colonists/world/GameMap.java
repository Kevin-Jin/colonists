package in.kevinj.colonists.world;

import in.kevinj.colonists.Player;

import java.util.BitSet;
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
	public static final int NUM_PLAYERS = 3;

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
	public void endTurn();

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
				if (entry.getKey().getType() == Coordinate.Type.EDGE)
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
			for (int i = 0; i < NUM_PLAYERS; i++)
				if (!map.getPlayer(i).availableMoves.equals(correctMoves.get(i)))
					return false;
			return true;
		}

		private static void dumpAvailableMoveDiff(GameMap<?> map) {
			List<Set<Coordinate.NegativeSpace>> exp = availableMovesCleanUpdate(map);
			for (int i = 0; i < NUM_PLAYERS; i++) {
				if (exp.get(i).equals(map.getPlayer(i).availableMoves)) {
					System.err.println("EQU " + i + ": " + exp.get(i));
				} else {
					System.err.println("EXP " + i + ": " + exp.get(i));
					System.err.println("ACT " + i + ": " + map.getPlayer(i).availableMoves);
					Set<Coordinate.NegativeSpace> diff = new HashSet<Coordinate.NegativeSpace>(exp.get(i));
					diff.removeAll(map.getPlayer(i).availableMoves);
					if (!diff.isEmpty())
						System.err.println("MIS " + i + ": " + diff);
					diff = new HashSet<Coordinate.NegativeSpace>(map.getPlayer(i).availableMoves);
					diff.removeAll(exp.get(i));
					if (!diff.isEmpty())
						System.err.println("EXT " + i + ": " + diff);
				}
			}
		}

		private static void dumpMapState(GameMap<?> map, Coordinate.NegativeSpace loc, String additionalLine) {
			Map<Coordinate.NegativeSpace, ? extends Entity.NegativeSpace> grid = map.getGrid();
			for (Map.Entry<Coordinate.NegativeSpace, ? extends Entity.NegativeSpace> entry : grid.entrySet())
				if (entry.getKey().getType() == Coordinate.Type.EDGE)
					System.err.println("grid.put(Coordinate.NegativeSpace.valueOf(" + entry.getKey().xHundreds() + ", " + entry.getKey()	.yHundreds() + "), new GraphicalEntity.Road(this, " + entry.getValue().getPlayer() + "));");
				else
					System.err.println("grid.put(Coordinate.NegativeSpace.valueOf(" + entry.getKey().xHundreds() + ", " + entry.getKey().yHundreds() + "), new GraphicalEntity.Metro(this, " + entry.getValue().getPlayer() + "));");
			System.err.println(additionalLine);
			System.err.println("currentPlayerTurn = " + map.getCurrentPlayerTurn() + "; for (Map.Entry<Coordinate.NegativeSpace, GraphicalEntity.NegativeSpace> entry : grid.entrySet()) entry.getValue().setPosition(entry.getKey()); java.util.List<java.util.Set<Coordinate.NegativeSpace>> initialAvailableMoves = GameMap.Helper.availableMovesCleanUpdate(this); for (int i = 0; i < NUM_PLAYERS; i++) { initializeGridStages[i] = 5; players[i].availableMoves.clear(); players[i].availableMoves.addAll(initialAvailableMoves.get(i)); }");
		}

		public static Integer initialAddToGrid(GameMap<?> map, Coordinate.NegativeSpace loc, int playerNum, int stage, Set<Coordinate.NegativeSpace> availableVertices) {
			Map<Coordinate.NegativeSpace, ? extends Entity.NegativeSpace> grid = map.getGrid();
			Player player = map.getPlayer(playerNum);
			switch (stage) {
				case 0: //before placing first village
				case 2: //after placing first road
				{
					player.availableMoves.clear();

					//assign to available the set of all empty, in-bounds, non-adjacent vertices in the map
					player.availableMoves.addAll(availableVertices);

					//TODO: could start on a player besides 0
					if (playerNum == NUM_PLAYERS - 1)
						//reverse direction
						return Integer.valueOf(0);
					return Integer.valueOf(1);
				}
				case 1: //after placing first village
				case 3: //after placing second village
				{
					player.availableMoves.clear();

					//assign to available the set of all empty, in-bounds edges leading out of loc
					for (Coordinate.NegativeSpace edge : loc.adjacentEdges())
						if (edge.inBounds() && !grid.containsKey(edge))
							player.availableMoves.add(edge);
					//make sure others can't pick this vertex or adjacent vertices
					List<Coordinate.NegativeSpace> neighbors = loc.adjacentVertices();
					for (int i = 0; i < NUM_PLAYERS; i++) {
						Player p = map.getPlayer(i);
						for (Coordinate.NegativeSpace vertex : neighbors)
							p.availableMoves.remove(vertex);
						p.availableMoves.remove(loc);
					}
					for (Coordinate.NegativeSpace vertex : neighbors)
						availableVertices.remove(vertex);
					availableVertices.remove(loc);

					return Integer.valueOf(0);
				}
				case 4: //after placing second road
				{
					player.availableMoves.clear();

					//TODO: could start on a player besides 0
					if (playerNum == 0) { //after everyone's placed their villages and roads
						//make sure others know their valid moves
						List<Set<Coordinate.NegativeSpace>> initialAvailableMoves = availableMovesCleanUpdate(map);
						for (int i = 0; i < NUM_PLAYERS; i++)
							map.getPlayer(i).availableMoves.addAll(initialAvailableMoves.get(i));

						//first player to place a village is the first to play
						return Integer.valueOf(0);
					}
					return Integer.valueOf(-1);
				}
				default:
					return null;
			}
		}

		public static void incrementalUpdateAfterAddToGrid(GameMap<?> map, Coordinate.NegativeSpace loc, Entity.NegativeSpace added) {
			Map<Coordinate.NegativeSpace, ? extends Entity.NegativeSpace> grid = map.getGrid();
			Entity.NegativeSpace tmp, neighborSquaredEnt;
			for (int i = 0; i < NUM_PLAYERS; i++)
				map.getPlayer(i).availableMoves.remove(loc);
			boolean isOrphaned = true, isSettlementError = false;
			if (loc.getType() == Coordinate.Type.EDGE) {
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
				BitSet connectedTo = new BitSet(NUM_PLAYERS);
				for (Coordinate.NegativeSpace edge : loc.adjacentEdges()) {
					if (grid.containsKey(edge)) {
						isOrphaned = false;
					} else if (edge.inBounds()) {
						connectedTo.clear();
						for (Coordinate.NegativeSpace neighborSquared : edge.adjacentEdges())
							if ((neighborSquaredEnt = grid.get(neighborSquared)) != null)
								//can't build a road through another user's settlement
								if ((tmp = grid.get(Coordinate.NegativeSpace.intersection(edge, neighborSquared))) == null || tmp.getPlayer() == neighborSquaredEnt.getPlayer())
									connectedTo.set(neighborSquaredEnt.getPlayer());
						for (Coordinate.NegativeSpace neighborSquared : edge.adjacentVertices())
							if ((neighborSquaredEnt = grid.get(neighborSquared)) != null)
								connectedTo.set(neighborSquaredEnt.getPlayer());
						for (int i = 0; i < NUM_PLAYERS; i++)
							if (connectedTo.get(i))
								map.getPlayer(i).availableMoves.add(edge);
							else
								map.getPlayer(i).availableMoves.remove(edge);
					}
				}
				for (Coordinate.NegativeSpace neighbor : loc.adjacentVertices())
					if (grid.containsKey(neighbor))
						isSettlementError = true;
					else for (int i = 0; i < NUM_PLAYERS; i++)
						map.getPlayer(i).availableMoves.remove(neighbor);
			}
			if (isOrphaned || isSettlementError)
				//graph is invalid
				for (int i = 0; i < NUM_PLAYERS; i++)
					map.getPlayer(i).availableMoves.clear();
			try {
				assert isConsistent(map);
			} catch (AssertionError e) {
				System.err.println("INCONSISTENCY WHEN ADDING " + loc);
				dumpAvailableMoveDiff(map);
				dumpMapState(map, loc, "grid.remove(Coordinate.NegativeSpace.valueOf(" + loc.xHundreds() + ", " + loc.yHundreds() + "));");
				throw e;
			}
		}

		public static void incrementalUpdateAfterRemoveFromGrid(GameMap<?> map, Coordinate.NegativeSpace loc, Entity.NegativeSpace removed) {
			Map<Coordinate.NegativeSpace, ? extends Entity.NegativeSpace> grid = map.getGrid();
			Entity.NegativeSpace tmp, neighborEnt;
			BitSet connectedTo = new BitSet(NUM_PLAYERS);
			boolean isOrphaned = false;
			if (loc.getType() == Coordinate.Type.EDGE) {
				//deleted an edge. invalidate any moves to orphaned unoccupied
				//"vertices"/"edges" and clear all moves if any occupied
				//"vertices"/"edges" are orphaned. add opened up move to edge.
				boolean isCutEdge;
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
							connectedTo.set(neighborEnt.getPlayer());
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
						connectedTo.set(neighborEnt.getPlayer());
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
					for (int i = 0; i < NUM_PLAYERS; i++)
						map.getPlayer(i).availableMoves.clear();
				else for (int player = connectedTo.nextSetBit(0); player != -1; player = connectedTo.nextSetBit(player + 1))
					//the removed edge can be reached from player's own "edges"/"vertices"
					map.getPlayer(player).availableMoves.add(loc);
			} else {
				//deleted a vertex. clear all moves if any occupied "edges" are
				//orphaned. add opened up moves to vertices
				boolean isCutVertex;
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
						connectedTo.set(neighborEnt.getPlayer());
						if (isCutVertex)
							isOrphaned = true;
					}
					//if vertex is removed, neighboring edge must either be orphaned
					//or connected to another of our edges, so availableMoves not changed
				}
				if (isOrphaned)
					//graph is invalid
					for (int i = 0; i < NUM_PLAYERS; i++)
						map.getPlayer(i).availableMoves.clear();
				else for (int player = connectedTo.nextSetBit(0); player != -1; player = connectedTo.nextSetBit(player + 1))
					//the removed vertex can be reached from player's own "edges"
					map.getPlayer(player).availableMoves.add(loc);
				//elimination of one vertex may open up adjacent vertices
				for (Iterator<Coordinate.NegativeSpace> iter = loc.adjacentVertices().iterator(); !isOrphaned && iter.hasNext(); ) {
					Coordinate.NegativeSpace neighbor = iter.next();
					connectedTo.clear();
					//get set of players with edges neighboring the neighboring vertex
					for (Coordinate.NegativeSpace otherEdge : neighbor.adjacentEdges()) {
						if ((tmp = grid.get(otherEdge)) != null) {
							connectedTo.set(tmp.getPlayer());
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
					for (int player = connectedTo.nextSetBit(0); player != -1; player = connectedTo.nextSetBit(player + 1))
						map.getPlayer(player).availableMoves.add(neighbor);
				}
			}
			try {
				assert isConsistent(map);
			} catch (AssertionError e) {
				System.err.println("INCONSISTENCY WHEN REMOVING " + loc);
				dumpAvailableMoveDiff(map);
				dumpMapState(map, loc, "");
				throw e;
			}
		}
	}
}
