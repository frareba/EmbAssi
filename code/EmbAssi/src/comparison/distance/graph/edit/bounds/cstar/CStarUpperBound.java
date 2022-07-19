package comparison.distance.graph.edit.bounds.cstar;

import comparison.distance.graph.edit.GraphEditDistanceAssignment;
import comparison.distance.graph.edit.GraphEditDistanceAssignmentValue;
import comparison.distance.graph.edit.bounds.GraphEditDistanceUpperBound;
import comparison.distance.graph.edit.bounds.refinement.Assignmentbased;
import comparison.distance.graph.edit.costs.GraphEditCosts;
import comparison.distance.graph.edit.search.cstar.StarEditDistance;
import datastructure.Pair;
import graph.LGraph;

public class CStarUpperBound<V,E> implements GraphEditDistanceUpperBound<V,E>,Assignmentbased<V,E> {

	GraphEditDistanceAssignmentValue<V,E> gedAssign;
	GraphEditCosts<V, E> graphEditCosts;
	public CStarUpperBound(GraphEditCosts<V, E> graphEditCosts)
	{
		this.gedAssign = new GraphEditDistanceAssignmentValue<>(new StarEditDistance<>());
		this.graphEditCosts = graphEditCosts;
	}
	@Override
	public double compute(LGraph<V, E> g1, LGraph<V, E> g2) {
		Pair<Double, int[]> p = gedAssign.computeEditPath(g1, g2);
		int[] editPath = p.getSecond();
		double bound = GraphEditDistanceAssignment.editCosts(g1, g2, graphEditCosts, editPath);
		return bound;
	}
	@Override
	public int[] getAssignment(LGraph<V, E> g1, LGraph<V, E> g2) {
		return gedAssign.computeEditPath(g1, g2).getSecond();
	}

}
