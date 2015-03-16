package in.kevinj.colonists.client;

import in.kevinj.colonists.client.world.Coordinate;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class GraphUtil {
	public static Set<Coordinate.NegativeSpace> dfsForAvailable(Set<Coordinate.NegativeSpace> vertices, Set<Coordinate.NegativeSpace> edges, Set<Coordinate.NegativeSpace> obstacles) {
		Set<Coordinate.NegativeSpace> available = new HashSet<Coordinate.NegativeSpace>();

		Set<Coordinate.NegativeSpace> visitedVertices = new HashSet<Coordinate.NegativeSpace>();
		Set<Coordinate.NegativeSpace> orphanedEdges = new HashSet<Coordinate.NegativeSpace>(edges);
		Stack<Coordinate.NegativeSpace> s = new Stack<Coordinate.NegativeSpace>();
		for (Coordinate.NegativeSpace vertex : vertices) {
			if (visitedVertices.contains(vertex))
				continue;

			visitedVertices.add(vertex);
//			visit(vertex);
			s.push(vertex);
			while (!s.isEmpty()) {
				vertex = s.pop();
				for (Coordinate.NegativeSpace neighbor : vertex.adjacentVertices()) {
					Coordinate.NegativeSpace edge = Coordinate.NegativeSpace.edge(vertex, neighbor);
					orphanedEdges.remove(edge);
					//this is part of our real visit. avoid a second loop over adjacent vertices in visit() by placing our logic here instead
					if (!edges.contains(edge) && !obstacles.contains(edge) && edge.inBounds())
						available.add(edge);

					if (vertices.contains(neighbor)) {
						//traverse "children"
						if (!visitedVertices.contains(neighbor)) {
							visitedVertices.add(neighbor);
//							visit(neighbor);
							s.push(neighbor);
						}
					} else if (!obstacles.contains(neighbor)) {
						//this is part of our real visit. avoid a second loop over adjacent vertices in visit() by placing our logic here instead
						if (edges.contains(edge) && !obstacles.contains(edge) && neighbor.inBounds())
							available.add(neighbor);
					}
				}
			}
		}
		for (Coordinate.NegativeSpace edge : orphanedEdges)
			for (Coordinate.NegativeSpace neighbor : Coordinate.NegativeSpace.vertices(edge))
				if (!vertices.contains(neighbor) && !obstacles.contains(neighbor))
					if (!obstacles.contains(edge) && neighbor.inBounds())
						available.add(neighbor);

		return available;
	}
}
