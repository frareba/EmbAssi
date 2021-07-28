package comparison.distance.graph.edit.bounds.beamsearch;

import comparison.distance.graph.edit.bounds.GraphEditDistanceUpperBound;
import comparison.distance.graph.edit.bounds.refinement.Assignmentbased;
import comparison.distance.graph.edit.costs.GraphEditCosts;
import graph.LGraph;

/**
 * Approximates the graph edit distance using beam search (Source: https://github.com/dan-zam/graph-matching-toolkit).
 * 
 * @author bause
 * 
 * @param <V>
 * @param <E>
 */
public class GraphEditBeamSearchDistance<V,E> implements GraphEditDistanceUpperBound<V,E>, Assignmentbased<V,E>{

	private ExactEditDistance<V,E> eDist;
	
	/**
	 * @param graphEditCosts costfunction to be used
	 * @param s max size of open-list
	 */
	public GraphEditBeamSearchDistance(GraphEditCosts<V,E> graphEditCosts, int s)
	{
		this.eDist = new ExactEditDistance<V,E>(graphEditCosts, s);
	}
	
	@Override
	public double compute(LGraph<V, E> g1, LGraph<V, E> g2) {
		return this.eDist.getEditDistance(g1, g2, Double.MAX_VALUE);
	}

	@Override
	public int[] getAssignment(LGraph<V, E> g1, LGraph<V, E> g2) {
		// TODO Auto-generated method stub
		System.err.println("Not yet implemented!");
		return null;
	}

}
