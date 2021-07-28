package comparison.distance.graph.edit.bounds.refinement;

import comparison.distance.graph.edit.GraphEditDistanceAssignment;
import comparison.distance.graph.edit.costs.GraphEditCosts;
import graph.LGraph;

/**
 * 
 * Implements the local search algorithm for refining an edit path (upper bound) as described in the paper 
 * 
 * Zhiping Zeng, Anthony K. H. Tung, Jianyong Wang, Jianhua Feng, Lizhu Zhou:
 * Comparing Stars: On Approximating Graph Edit Distance. PVLDB 2(1): 25-36 (2009)
 * 
 * Tries to improve the upper bound by swapping two assigned vertices.
 * Is very similar to k-refine with k=2 (not exactly the same).
 * 
 * @author kriege/bause
 *
 * @param <V>
 * @param <E>
 */
public class SimpleLocalSearch<V,E> extends RefinementAlgorithm<V,E>{
	
	public int[] refinementStep(LGraph<V, E> lgEdit, LGraph<V, E> lgTarget, GraphEditCosts<V,E> graphEditCosts, int[] editPath) {
		int[] bestEditPath = null;
		double bestCost = Integer.MAX_VALUE;
		for (int i=0; i<editPath.length; i++) {
			for (int j=i+1; j<editPath.length; j++) {
				// swap
				int tmp = editPath[i];
				editPath[i] = editPath[j];
				editPath[j] = tmp;
				
				// check costs and store
				double cost = GraphEditDistanceAssignment.editCosts(lgEdit, lgTarget, graphEditCosts, editPath);
				if (cost<bestCost) {
					bestCost = cost;
					bestEditPath = editPath.clone();
				}
				
				// unswap
				editPath[j] = editPath[i];
				editPath[i] = tmp;
			}
		}
		return bestEditPath;
	}

	@Override
	public int[] refine(LGraph<V, E> lgEdit, LGraph<V, E> lgTarget, GraphEditCosts<V, E> graphEditCosts,
			int[] editPath) {
		double cost = GraphEditDistanceAssignment.editCosts(lgEdit, lgTarget, graphEditCosts, editPath);

		while (true) {
			int[] newEditPath = refinementStep(lgEdit, lgTarget, graphEditCosts, editPath);
			double newCost = GraphEditDistanceAssignment.editCosts(lgEdit, lgTarget, graphEditCosts, newEditPath);
			if (newCost < cost) {
				editPath = newEditPath;
				cost = newCost; // GraphEditDistanceAssignment.editCosts(lgEdit, lgTarget, graphEditCosts, editPath);
			} else {
				break;
			}
		}
		
		return editPath;
	}

}
