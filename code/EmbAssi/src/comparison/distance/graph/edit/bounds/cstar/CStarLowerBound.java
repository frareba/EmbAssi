package comparison.distance.graph.edit.bounds.cstar;

import comparison.distance.graph.edit.GraphEditDistanceAssignmentValue;
import comparison.distance.graph.edit.bounds.GraphEditDistanceLowerBound;
import comparison.distance.graph.edit.search.cstar.StarEditDistance;
import datastructure.Pair;
import graph.GraphTools;
import graph.LGraph;

public class CStarLowerBound<V,E> implements GraphEditDistanceLowerBound<V,E> {
	
	GraphEditDistanceAssignmentValue<V,E> gedAssign;
	
	public CStarLowerBound()
	{
		this.gedAssign = new GraphEditDistanceAssignmentValue<>(new StarEditDistance<>());
	}
	@Override
	public double compute(LGraph<V, E> g1, LGraph<V, E> g2) {
		Pair<Double, int[]> p = gedAssign.computeEditPath(g1, g2);
		double bound = p.getFirst();
		// make lower bound
		int maxDeg1 = GraphTools.maximumDegree(g1.getGraph());
		int maxDeg2 = GraphTools.maximumDegree(g2.getGraph());
		bound /= Math.max(4, Math.max(maxDeg1, maxDeg2)+1);
		return bound;
	}

}
