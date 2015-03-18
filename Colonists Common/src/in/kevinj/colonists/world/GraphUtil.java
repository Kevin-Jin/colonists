package in.kevinj.colonists.world;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class GraphUtil {
	private static class SimpleEntry<K, V> implements Map.Entry<K, V> {
		private final K key;
		private V value;

		public SimpleEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			V old = this.value;
			this.value = value;
			return old;
		}
	}

	private static void and(Map<Coordinate.NegativeSpace, BitSet> map, Coordinate.NegativeSpace key, int value) {
		if (value == -1) {
			BitSet validForPlayers = map.get(key);
			if (validForPlayers == null)
				map.put(key, new BitSet(GameMap.NUM_PLAYERS));
			else
				validForPlayers.clear();
		} else {
			BitSet validForPlayers = map.get(key);
			if (validForPlayers == null) {
				validForPlayers = new BitSet(GameMap.NUM_PLAYERS);
				map.put(key, validForPlayers);
				validForPlayers.set(value);
			} else if (!validForPlayers.isEmpty()) {
				validForPlayers.set(value);
			}
		}
	}

	private static List<Set<Coordinate.NegativeSpace>> makeEmptyList(List<Set<Coordinate.NegativeSpace>> existing) {
		for (int i = 0; i < GameMap.NUM_PLAYERS; i++)
			existing.set(i, Collections.<Coordinate.NegativeSpace>emptySet());
		return existing;
	}

	public static List<Set<Coordinate.NegativeSpace>> searchForAvailable(Map<Coordinate.NegativeSpace, Entity.NegativeSpace> vertices, Map<Coordinate.NegativeSpace, Entity.NegativeSpace> edges) {
		List<Set<Coordinate.NegativeSpace>> available = new ArrayList<Set<Coordinate.NegativeSpace>>(GameMap.NUM_PLAYERS);
		for (int i = 0; i < GameMap.NUM_PLAYERS; i++)
			available.add(new HashSet<Coordinate.NegativeSpace>());

		//could use a BitSet too since integral values are in [0, 6]
		Map<Coordinate.NegativeSpace, BitSet> validVertices = new HashMap<Coordinate.NegativeSpace, BitSet>();
		Set<Coordinate.NegativeSpace> visitedEdges = new HashSet<Coordinate.NegativeSpace>();
		Set<Coordinate.NegativeSpace> visitedVertices = new HashSet<Coordinate.NegativeSpace>();

		//highly confusing, but we're performing a depth first search on a graph
		//whose vertices are our map's "edges" and whose edges are our map's
		//"edges" and "vertices"
		Stack<Map.Entry<Coordinate.NegativeSpace, Entity.NegativeSpace>> s = new Stack<Map.Entry<Coordinate.NegativeSpace, Entity.NegativeSpace>>();
		Entity.NegativeSpace tmp;
		//graph can be disconnected, so loop over all of our map's "edges" to
		//get our roots
		for (Map.Entry<Coordinate.NegativeSpace, Entity.NegativeSpace> sourceEdgeEntry : edges.entrySet()) {
			Coordinate.NegativeSpace edge = sourceEdgeEntry.getKey();
			if (visitedEdges.contains(edge))
				continue;

			visitedEdges.add(edge);
			s.push(sourceEdgeEntry);

			while (!s.isEmpty()) {
				Map.Entry<Coordinate.NegativeSpace, Entity.NegativeSpace> edgeEntry = s.pop();
				edge = edgeEntry.getKey();
				int player = edgeEntry.getValue().getPlayer();

				List<Coordinate.NegativeSpace> adjVertices = edge.adjacentVertices();
				assert adjVertices.size() == 2;
				boolean vertex0 = vertices.containsKey(adjVertices.get(0));
				boolean vertex1 = vertices.containsKey(adjVertices.get(1));
				if (vertex0 && vertex1) {
					//settlement next to another settlement
					return makeEmptyList(available);
				} else if (vertex0 || vertex1) {
					//if one "vertex" contains a settlement, neither "vertex"
					//can accommodate another settlement
					and(validVertices, adjVertices.get(0), -1);
					and(validVertices, adjVertices.get(1), -1);
					//keep track of settlement-containing "vertices" we visited
					if (vertex0)
						visitedVertices.add(adjVertices.get(0));
					else
						visitedVertices.add(adjVertices.get(1));
				} else {
					//neither "vertex" contains a settlement. tentatively say
					//both "vertices" can accommodate a settlement until we are
					//proved wrong by a second visit on the "vertex"
					and(validVertices, adjVertices.get(0), player);
					and(validVertices, adjVertices.get(1), player);
				}

				boolean isOrphaned = true;
				for (Coordinate.NegativeSpace neighborEdge : edge.adjacentEdges()) {
					Entity.NegativeSpace neighborEnt = edges.get(neighborEdge);
					isOrphaned = isOrphaned && (neighborEnt == null);
					if (neighborEnt == null) {
						if (neighborEdge.inBounds())
							//can't build a road through another user's settlement
							if ((tmp = vertices.get(Coordinate.NegativeSpace.intersection(edge, neighborEdge))) == null || tmp.getPlayer() == player)
								available.get(player).add(neighborEdge);

						adjVertices = neighborEdge.adjacentVertices();
						assert adjVertices.size() == 2;
						vertex0 = vertices.containsKey(adjVertices.get(0));
						vertex1 = vertices.containsKey(adjVertices.get(1));
						if (vertex0 && vertex1) {
							//settlement next to another settlement
							return makeEmptyList(available);
						} else if (vertex0 || vertex1) {
							//if one "vertex" contains a settlement, neither
							//"vertex" can accommodate another settlement
							and(validVertices, adjVertices.get(0), -1);
							and(validVertices, adjVertices.get(1), -1);
						}
						isOrphaned = isOrphaned && !vertices.containsKey(Coordinate.NegativeSpace.intersection(neighborEdge, edge));
					} else if (!visitedEdges.contains(neighborEdge)) {
						visitedEdges.add(neighborEdge);
						s.push(new SimpleEntry<Coordinate.NegativeSpace, Entity.NegativeSpace>(neighborEdge, neighborEnt));
					}
				}
				if (isOrphaned)
					//edge without an adjacent edge or vertex
					return makeEmptyList(available);
			}
		}
		if (visitedVertices.size() < vertices.size())
			//vertex without an adjacent edge
			return makeEmptyList(available);

		for (Map.Entry<Coordinate.NegativeSpace, BitSet> validVertex : validVertices.entrySet())
			if (validVertex.getKey().inBounds())
				for (int player = validVertex.getValue().nextSetBit(0); player != -1; player = validVertex.getValue().nextSetBit(player + 1))
					available.get(player).add(validVertex.getKey());

		return available;
	}
}
