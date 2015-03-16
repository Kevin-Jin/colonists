package in.kevinj.colonists.world;

import java.util.ArrayList;
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

	//TODO: can't have a vertex within two edges of any other vertex. Maybe BFS is better suited for this since it visits nodes in increasing distance from "source"?
	//TODO: flag for board initialization stage, i.e. no need for connecting edges when searching for available vertices
	public static List<Set<Coordinate.NegativeSpace>> dfsForAvailable(Map<Coordinate.NegativeSpace, Entity.NegativeSpace> vertices, Map<Coordinate.NegativeSpace, Entity.NegativeSpace> edges) {
		List<Set<Coordinate.NegativeSpace>> available = new ArrayList<Set<Coordinate.NegativeSpace>>(4);
		for (int i = 0; i < 4; i++)
			available.add(new HashSet<Coordinate.NegativeSpace>());

		Set<Coordinate.NegativeSpace> visitedVertices = new HashSet<Coordinate.NegativeSpace>();
		Map<Coordinate.NegativeSpace, Entity.NegativeSpace> orphanedEdges = new HashMap<Coordinate.NegativeSpace, Entity.NegativeSpace>(edges);
		Stack<Map.Entry<Coordinate.NegativeSpace, Entity.NegativeSpace>> s = new Stack<Map.Entry<Coordinate.NegativeSpace, Entity.NegativeSpace>>();
		for (Map.Entry<Coordinate.NegativeSpace, Entity.NegativeSpace> entry : vertices.entrySet()) {
			Coordinate.NegativeSpace vertex = entry.getKey();
			if (visitedVertices.contains(vertex))
				continue;

			visitedVertices.add(vertex);
//			visit(vertex);
			s.push(entry);
			while (!s.isEmpty()) {
				Map.Entry<Coordinate.NegativeSpace, Entity.NegativeSpace> stackEntry = s.pop();
				vertex = stackEntry.getKey();
				for (Coordinate.NegativeSpace neighbor : vertex.adjacentVertices()) {
					Coordinate.NegativeSpace edge = Coordinate.NegativeSpace.edge(vertex, neighbor);
					orphanedEdges.remove(edge);
					//this is part of our real visit. avoid a second loop over adjacent vertices in visit() by placing our logic here instead
					if (!edges.containsKey(edge) && edge.inBounds())
						//player who owns the neighboring vertex has access to this empty edge
						available.get(stackEntry.getValue().getPlayer()).add(edge);

					Entity.NegativeSpace neighborEnt = vertices.get(neighbor);
					if (neighborEnt != null) {
						//traverse "children"
						if (!visitedVertices.contains(neighbor)) {
							visitedVertices.add(neighbor);
//							visit(neighbor);
							s.push(new SimpleEntry<Coordinate.NegativeSpace, Entity.NegativeSpace>(neighbor, neighborEnt));
						}
					} else {
						//this is part of our real visit. avoid a second loop over adjacent vertices in visit() by placing our logic here instead
						Entity.NegativeSpace edgeEnt = edges.get(edge);
						if (edgeEnt != null && neighbor.inBounds())
							//player who owns the neighboring edge has access to this empty vertex
							available.get(edgeEnt.getPlayer()).add(neighbor);
					}
				}
			}
		}
		for (Map.Entry<Coordinate.NegativeSpace, Entity.NegativeSpace> edgeEntry : orphanedEdges.entrySet())
			for (Coordinate.NegativeSpace neighbor : Coordinate.NegativeSpace.vertices(edgeEntry.getKey()))
				if (!vertices.containsKey(neighbor) && neighbor.inBounds())
					//player who owns the neighboring edge has access to this empty vertex
					available.get(edgeEntry.getValue().getPlayer()).add(neighbor);

		return available;
	}
}
