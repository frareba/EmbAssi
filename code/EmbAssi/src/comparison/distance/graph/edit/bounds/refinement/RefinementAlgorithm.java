package comparison.distance.graph.edit.bounds.refinement;

import comparison.distance.graph.edit.GraphEditDistanceAssignment;
import comparison.distance.graph.edit.costs.GraphEditCosts;
import graph.LGraph;

/**
 * Abstract class for classes, that improve a given
 * vertex assignment (edit path) to gain better upper bounds for the graph edit distance.
 * 
 * @author bause
 *
 * @param <V>
 * @param <E>
 */
public abstract class RefinementAlgorithm<V,E> {

	/**
	 * Improves an edit path and returns it.
	 * @param lgEdit first graph
	 * @param lgTarget second graph
	 * @param graphEditCosts underlying edit costs
	 * @param editPath assignment of vertices of lgEdit to vertices of lgTarget: vertex i is assigned to vertex editPath[i] 
	 * @return the improved edit path between lgEdit and lgTarget
	 */
	public abstract int[] refine(LGraph<V, E> lgEdit, LGraph<V, E> lgTarget, GraphEditCosts<V,E> graphEditCosts, int[] editPath); 

	/**
	 * Improves an edit path using the corresponding refine method and returns its cost.
	 * @param lgEdit first graph
	 * @param lgTarget second graph
	 * @param graphEditCosts underlying edit costs
	 * @param editPath assignment of vertices of lgEdit to vertices of lgTarget: vertex i is assigned to vertex editPath[i] 
	 * @return cost of the improved edit path between lgEdit and lgTarget
	 */
	public double refinedCost(LGraph<V, E> lgEdit, LGraph<V, E> lgTarget, GraphEditCosts<V,E> graphEditCosts, int[] editPath) {
		int[] refinedEditPath= refine(lgEdit, lgTarget, graphEditCosts, editPath);
		return GraphEditDistanceAssignment.editCosts(lgEdit, lgTarget, graphEditCosts, refinedEditPath);
	}
	/**
	 * Just a wrapper to get the cost of an edit path
	 * @param lgEdit
	 * @param lgTarget
	 * @param graphEditCosts
	 * @param editPath
	 * @return
	 */
	public double getCost(LGraph<V, E> lgEdit, LGraph<V, E> lgTarget, GraphEditCosts<V,E> graphEditCosts, int[] editPath) {
		return GraphEditDistanceAssignment.editCosts(lgEdit, lgTarget, graphEditCosts, editPath);
	}
}
