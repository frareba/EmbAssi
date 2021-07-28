package comparison.distance.graph.edit.vectordistance.distances;

import java.util.ArrayList;
import java.util.HashMap;
import comparison.distance.tree.TreeDistance;
import datastructure.SparseFeatureVector;
import graph.Graph.Vertex;
import graph.LGraph;

/**
 * @author bause
 * GraphEditCostsVectorDistance, where the vectors are based on some combination of TreeDistances (only vertices)
 * @param <V>
 * @param <E>
 */
public class GraphEditCostsCombinedVectorDistance<V,E> extends GraphEditCostsVectorDistance<V,E,Vertex>{

	private ArrayList<TreeDistance<Vertex>> dists;
	private ArrayList<GraphEditCostsVertexVectorDistance<V,E>> vecDists;
	
	public GraphEditCostsCombinedVectorDistance(ArrayList<LGraph<V, E>> lgs, ArrayList<TreeDistance<Vertex>> dists) {
		super(lgs, dists);
		this.dists = dists;
		this.vectorMap = new HashMap<LGraph<V,E>,SparseFeatureVector<Integer>>();
		this.lgs = lgs;
		this.vecDists = new ArrayList<GraphEditCostsVertexVectorDistance<V,E>>();
		for(TreeDistance<Vertex> distance: dists)
		{
			this.vecDists.add(new GraphEditCostsVertexVectorDistance<V,E>(lgs, distance));
		}
		
		// build vectors
		fillVectorMap();
		
		//set max entry
		this.setMaxEntry();
	}

	private void setMaxEntry() {
		for(SparseFeatureVector<Integer> vec : this.vectorMap.values())
		{
			for(int i :vec.nonZeroFeatures())
			{
				if(i> this.maxEntry)
				{
					this.maxEntry = i;
				}
			}
		}
	}

	@Override
	protected void insertDummyVertices() {
	}

	@Override
	protected void setMaxSetSize() {
	}
	@Override
	protected void fillVectorMap() {
		for(LGraph<V,E> graph: this.lgs)
		{
			SparseFeatureVector<Integer> vector = getVec(graph);
			this.vectorMap.put(graph, vector);
		}
	}

	/**
	 * @param graph
	 * @return computes a combined vector from the vectors of the underlying distances
	 */
	private SparseFeatureVector<Integer> getVec(LGraph<V, E> graph) {
		SparseFeatureVector<Integer> vector = new SparseFeatureVector<Integer>();
		int offset = 0;
		for(GraphEditCostsVertexVectorDistance<V,E> vecDist: this.vecDists)
		{
			SparseFeatureVector<Integer> singleVec= vecDist.getVectorMap().get(graph);
			for(Integer i: singleVec.nonZeroFeatures())
			{
				vector.setValue(i+offset,singleVec.getValue(i));
			}
			offset += vecDist.getMaxEntry()+1;
		}
		return vector;
	}

	@Override
	protected ArrayList<Vertex> getVertexSet(LGraph<V, E> graph) {
		System.err.println("Not yet implemented");
		return null;
	}

}
