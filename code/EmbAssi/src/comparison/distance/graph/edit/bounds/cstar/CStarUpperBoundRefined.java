package comparison.distance.graph.edit.bounds.cstar;

import comparison.distance.graph.edit.GraphEditDistanceAssignmentValue;
import comparison.distance.graph.edit.bounds.GraphEditDistanceUpperBound;
import comparison.distance.graph.edit.bounds.refinement.SimpleLocalSearch;
import comparison.distance.graph.edit.costs.GraphEditCosts;
import comparison.distance.graph.edit.search.cstar.StarEditDistance;
import datastructure.Pair;
import graph.LGraph;

public class CStarUpperBoundRefined<V,E> implements GraphEditDistanceUpperBound<V,E> {

	GraphEditDistanceAssignmentValue<V,E> gedAssign;
	SimpleLocalSearch<V, E> refinement;
	GraphEditCosts<V, E> graphEditCosts;
	public CStarUpperBoundRefined(GraphEditCosts<V, E> graphEditCosts)
	{
		this.gedAssign = new GraphEditDistanceAssignmentValue<>(new StarEditDistance<>());
		this.refinement = new SimpleLocalSearch<>();
		this.graphEditCosts = graphEditCosts;//new GraphEditCosts<>(1, 1, 1, 1, new ZeroDistance(), new IdentityDistance());
		//this.graphEditCosts = new GraphEditCosts<>();
		
	}
	@Override
	public double compute(LGraph<V, E> g1, LGraph<V, E> g2) {
		Pair<Double, int[]> p = gedAssign.computeEditPath(g1, g2);
		int[] editPath = p.getSecond();
		double bound = refinement.refinedCost(g1, g2, graphEditCosts, editPath);
		return bound;
	}

}
