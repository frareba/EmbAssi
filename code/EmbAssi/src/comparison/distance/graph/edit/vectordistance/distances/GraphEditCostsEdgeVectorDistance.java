package comparison.distance.graph.edit.vectordistance.distances;

import java.util.ArrayList;
import comparison.distance.tree.TreeDistance;
import graph.AdjListGraph;
import graph.LGraph;
import graph.Graph.Edge;
import graph.Graph.Vertex;


/**
 * @author bause
 * GraphEditCostsVectorDistance, where the vectors are based on the edges of the given graphs
 * @param <V>
 * @param <E>
 */
public class GraphEditCostsEdgeVectorDistance<V,E> extends GraphEditCostsVectorDistance<V,E,Edge>{
	public static Edge INSERTION_DUMMY; //no insertion dummy for edges was defined before (only for vertices)
	
	private int maxSetSize;
	
	/**
	 * @param lgs set of graphs for that the graph edit distance will be computed
	 * @param dist treedistance for generating the vectors
	 */
	public GraphEditCostsEdgeVectorDistance(ArrayList<LGraph<V,E>> lgs, TreeDistance<Edge> dist)
	{
		//initialize
		super(lgs, dist);
		AdjListGraph g = new AdjListGraph();
		Vertex v1 = g.createVertex();
		Vertex v2 = g.createVertex();
		Edge e = g.createEdge(v1, v2);
		INSERTION_DUMMY = e;
	}
	
	protected void insertDummyVertices() {
		for (LGraph<V, E> graph : this.lgs)
		{
			int n = graph.getGraph().getEdgeCount();
			int m = this.maxSetSize - n;
			ArrayList<Edge> A = new ArrayList<Edge>(n + m);
			for (Edge v : graph.getGraph().edges())
			{
				A.add(v);
			}

			// add dummy vertices for insertion/deletion
			for (int i = 0; i < m; i++)
			{
				A.add(INSERTION_DUMMY);
			}
			this.mapToVertexSets.put(graph, A);

		}
	}
	
	protected void setMaxSetSize() {
		this.maxSetSize=0;
		for(LGraph<V,E> graph: this.lgs)
		{
			if(graph.getGraph().getEdgeCount()>this.maxSetSize)
			{
				this.maxSetSize = graph.getGraph().getEdgeCount();
			}
		}
	}

	@Override
	protected ArrayList<Edge> getVertexSet(LGraph<V, E> graph) {
		System.err.println("Not yet implemented");
		return null;
	}
}
