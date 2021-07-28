package comparison.distance.graph.edit.bounds.branch;

import algorithm.assignment.AssignmentSolver;
import comparison.distance.graph.edit.GraphEditDistanceAssignmentValue;
import comparison.distance.graph.edit.bounds.GraphEditDistanceLowerBound;
import comparison.distance.graph.edit.costs.GraphEditAssignmentCosts;
import comparison.distance.graph.edit.costs.GraphEditAssignmentCostsExtended;
import comparison.distance.graph.edit.costs.GraphEditCosts;

/**
 * Implements the BRANCH heuristic as described in the paper:
 * 
 * David B. Blumenthal, Nicolas Boria, Johann Gamper, Sébastien Bougleux, Luc Brun:
 * Comparing heuristics for graph edit distance computation. VLDB J. 29(1): 419-458 (2020)
 * 
 * This is a variant of the BP algorithm and implemented by extending that implementation.
 * 
 * @author kriege
 *
 */
public class Branch<V,E> extends GraphEditDistanceAssignmentValue<V,E> implements GraphEditDistanceLowerBound<V,E> {

	AssignmentSolver solver;
	GraphEditAssignmentCosts<V,E> assignmentCosts;
	
	public Branch(GraphEditCosts<V,E> graphEditCosts) {
		super(new GraphEditAssignmentCostsExtended<>(graphEditCosts, 0.5));
	}




}
