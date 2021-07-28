package comparison.distance.graph.edit.bounds.refinement;

import graph.LGraph;

/**
 * 
 * Interface for all kinds of assignment-based approximations for the graph edit distance.
 * 
 * @author bause
 *
 * @param <V>
 * @param <E>
 */
public interface Assignmentbased<V,E> {
	/**
	 * @param g1
	 * @param g2
	 * @return an assignment of g1s vertices to g2s vertices, that might be improved by local search
	 */
	public int[] getAssignment(LGraph<V,E> g1,LGraph<V,E> g2);
	
}
