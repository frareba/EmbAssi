package comparison.distance.graph.edit.vectordistance.distances;

import java.util.ArrayList;

import comparison.distance.graph.edit.treedistance.GraphEditAssignmentCostsTreeDistance;
import comparison.distance.tree.TreeDistance;
import graph.LGraph;
import graph.Graph.Vertex;

/**
 * @author bause
 * GraphEditCostsVectorDistance, where the vectors are based on the vertices of the given graphs
 * @param <V>
 * @param <E>
 */
public class GraphEditCostsVertexVectorDistance<V,E> extends GraphEditCostsVectorDistance<V,E,Vertex>{
	
	/**
	 * @param lgs set of graphs for that the graph edit distance will be computed
	 * @param dist treedistance for generating the vectors
	 */
	public GraphEditCostsVertexVectorDistance(ArrayList<LGraph<V,E>> lgs, TreeDistance<Vertex> dist)
	{
		//initialize
		super(lgs, dist);
	}
	
	protected void insertDummyVertices() {
		for (LGraph<V, E> graph : this.lgs)
		{
			int n = graph.getGraph().getVertexCount();
			int m = this.maxSetSize - n;
			ArrayList<Vertex> A = new ArrayList<Vertex>(n + m);
			for (Vertex v : graph.getGraph().vertices())
			{
				A.add(v);
			}

			// add dummy vertices for insertion/deletion
			for (int i = 0; i < m; i++)
			{
				A.add(GraphEditAssignmentCostsTreeDistance.INSERTION_DUMMY);
			}
			this.mapToVertexSets.put(graph, A);

		}
	}
	
	protected void setMaxSetSize() {
		this.maxSetSize=0;
		for(LGraph<V,E> graph: this.lgs)
		{
			if(graph.getGraph().getVertexCount()>this.maxSetSize)
			{
				this.maxSetSize = graph.getGraph().getVertexCount();
			}
		}
	}

	@Override
	protected ArrayList<Vertex> getVertexSet(LGraph<V, E> graph) {
		System.err.println("Not yet implemented");
		return null;
	}
}
