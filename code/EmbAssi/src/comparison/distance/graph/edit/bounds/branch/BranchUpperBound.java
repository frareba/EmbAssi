package comparison.distance.graph.edit.bounds.branch;

import comparison.distance.graph.edit.GraphEditDistanceAssignment;
import comparison.distance.graph.edit.bounds.GraphEditDistanceUpperBound;
import comparison.distance.graph.edit.bounds.refinement.Assignmentbased;
import comparison.distance.graph.edit.costs.GraphEditCosts;
import graph.LGraph;

public class BranchUpperBound<V,E> implements GraphEditDistanceUpperBound<V,E>, Assignmentbased<V,E> {

	private Branch<V, E> branch;
	private GraphEditCosts<V,E> gec;
	public BranchUpperBound(GraphEditCosts<V,E> gec)
	{
		this.gec = gec;
		this.branch = new Branch<V,E>(gec);
	}
	@Override
	public double compute(LGraph<V, E> g1, LGraph<V, E> g2) {
		int[] assignment =  branch.computeEditPath(g1, g2).getSecond();
		return GraphEditDistanceAssignment.editCosts(g1, g2, this.gec, assignment);
	}
	
	@Override
	public int[] getAssignment(LGraph<V, E> g1, LGraph<V, E> g2)
	{
		return branch.computeEditPath(g1, g2).getSecond();
	}
	
	
}
