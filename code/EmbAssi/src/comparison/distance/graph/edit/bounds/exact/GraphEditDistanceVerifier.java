package comparison.distance.graph.edit.bounds.exact;

import java.util.Collection;

import graph.LGraph;

public interface GraphEditDistanceVerifier<V,E> {

	/**
	 * Computes the graph edit distance if it is lower or equal to the given bound.
	 * 
	 * @param q first graph
	 * @param g second graph
	 * @param bound bound on the graph edit distance
	 * @return the graph edit distance; -1 if the graph edit distance exceeds the bound
	 */
	public double compute(LGraph<V, E> q, LGraph<V, E> g, double bound);

	/**
	 * Returns the subset of the given dataset satisfying the graph edit distance
	 * bound.
	 * 
	 * @param q query graph
	 * @param ds dataset
	 * @param bound bound on the graph edit distance
	 * @return graphs in the dataset satisfying the bound
	 */
	public Collection<LGraph<V, E>> compute(LGraph<V, E> q, Collection<LGraph<V, E>> ds, double bound);
	
}
