package in.kevinj.colonists.client;

import in.kevinj.colonists.client.world.WorldModel;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class GraphUtil {
	public static Set<WorldModel.EntityCoordinate> dfsForAvailable(Set<WorldModel.EntityCoordinate> vertices, Set<WorldModel.EntityCoordinate> edges, Set<WorldModel.EntityCoordinate> obstacles) {
		Set<WorldModel.EntityCoordinate> available = new HashSet<WorldModel.EntityCoordinate>();

		Set<WorldModel.EntityCoordinate> visitedVertices = new HashSet<WorldModel.EntityCoordinate>();
		Set<WorldModel.EntityCoordinate> orphanedEdges = new HashSet<WorldModel.EntityCoordinate>(edges);
		Stack<WorldModel.EntityCoordinate> s = new Stack<WorldModel.EntityCoordinate>();
		for (WorldModel.EntityCoordinate vertex : vertices) {
			if (visitedVertices.contains(vertex))
				continue;

			visitedVertices.add(vertex);
//			visit(vertex);
			s.push(vertex);
			while (!s.isEmpty()) {
				vertex = s.pop();
				for (WorldModel.EntityCoordinate neighbor : vertex.adjacentVertices()) {
					WorldModel.EntityCoordinate edge = WorldModel.EntityCoordinate.edge(vertex, neighbor);
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
		for (WorldModel.EntityCoordinate edge : orphanedEdges)
			for (WorldModel.EntityCoordinate neighbor : WorldModel.EntityCoordinate.vertices(edge))
				if (!vertices.contains(neighbor) && !obstacles.contains(neighbor))
					if (!obstacles.contains(edge) && neighbor.inBounds())
						available.add(neighbor);

		return available;
	}
}
