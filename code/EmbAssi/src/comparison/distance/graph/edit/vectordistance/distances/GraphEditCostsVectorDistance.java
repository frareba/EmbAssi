package comparison.distance.graph.edit.vectordistance.distances;

import java.util.ArrayList;
import java.util.HashMap;

import comparison.distance.Distance;
import comparison.distance.ManhattanDistance;
import comparison.distance.tree.TreeDistance;
import datastructure.SparseFeatureVector;
import graph.LGraph;

/**
 * @author bause
 * abstract class for VectorDistances
 * @param <V>
 * @param <E>
 * @param <T>
 */
public abstract class GraphEditCostsVectorDistance<V,E,T> implements Distance<LGraph<V,E>>{

	protected int maxEntry;
	protected HashMap<LGraph<V, E>, SparseFeatureVector<Integer>> vectorMap;
	protected Vectors<T> vectors;
	protected ArrayList<? extends LGraph<V,E>> lgs;
	protected HashMap<LGraph<V,E>,ArrayList<T>> mapToVertexSets;
	protected TreeDistance<T> dist;
	protected int maxSetSize;
	
	/**
	 * @param lgs set of graphs for that the graph edit distance will be computed
	 * @param dist treedistance for generating the vectors
	 */
	public GraphEditCostsVectorDistance(ArrayList<LGraph<V,E>> lgs, TreeDistance<T> dist)
	{
		//initialize
		this.vectors = new Vectors<T>(dist);
		this.vectorMap = new HashMap<LGraph<V,E>,SparseFeatureVector<Integer>>();
		this.mapToVertexSets = new HashMap<LGraph<V,E>,ArrayList<T>>();
		this.lgs = lgs;
		this.maxEntry =dist.getRootedTree().getEdgeCount();
		this.dist = dist;
		
		setMaxSetSize();
		insertDummyVertices();
		fillVectorMap();
	}
	
	/**
	 * this should not be used..
	 * @param lgs
	 * @param dists
	 */
	public GraphEditCostsVectorDistance(ArrayList<LGraph<V,E>> lgs, ArrayList<TreeDistance<T>> dists)
	{}
	
	/**
	 * this should insert dummy vertices for all graphs, so that their vertexSets all are the same size
	 */
	protected abstract void insertDummyVertices();
	/**
	 * this should set maxSetSize to the maximum size of the vertexSets
	 * (and should be used for insertion of the right amount of dummy vertices)
	 */
	protected abstract void setMaxSetSize();
	
	/**
	 * @return the max filled position of a SparseFeatureVector<Integer>
	 */
	public int getMaxEntry()
	{
		return this.maxEntry;
	}
	/**
	 * @return a map from graph to its vector
	 */
	public HashMap<LGraph<V,E>,SparseFeatureVector<Integer>> getVectorMap()
	{
		return this.vectorMap;
	}

	@Override
	public double compute(LGraph<V, E> g1, LGraph<V, E> g2) {
		SparseFeatureVector<Integer> vec1 = getVector(g1);
		SparseFeatureVector<Integer> vec2 = getVector(g2);
		ManhattanDistance distance= new ManhattanDistance();
		return distance.compute(vec1,vec2);
	}
	
	public SparseFeatureVector<Integer> getVector(LGraph<V, E> graph)
	{
		if(!this.vectorMap.containsKey(graph))
		{
			SparseFeatureVector<Integer> vector = this.vectors.computeVector(this.getVertexSet(graph));
			this.vectorMap.put(graph, vector);
		}
		return this.vectorMap.get(graph);
	}

	protected abstract ArrayList<T> getVertexSet(LGraph<V, E> graph);

	/**
	 * fills vectorMap
	 */
	protected void fillVectorMap() {
		int i=0;
		for(LGraph<V,E> graph: this.lgs)
		{
			SparseFeatureVector<Integer> vector = this.vectors.computeVector(this.mapToVertexSets.get(graph));
			this.vectorMap.put(graph, vector);
		}
	}
}
