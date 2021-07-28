package comparison.distance.graph.edit.treedistance;

import java.util.ArrayList;
import comparison.distance.graph.edit.GraphEditDistanceAssignment;
import comparison.distance.graph.edit.bounds.GraphEditDistanceUpperBound;
import comparison.distance.graph.edit.bounds.refinement.Assignmentbased;
import comparison.distance.graph.edit.costs.GraphEditCosts;
import comparison.distance.tree.TreeDistance;
import comparison.distance.tree.TreeDistanceAssignmentSolver;
import datastructure.Pair;
import graph.Graph.Vertex;
import graph.LGraph;

/**
 * Approximates the graph edit distance in linear time by computing an optimal assignment between the vertices
 * and then deriving an edit path from that assignment.
 * The assignment is computed using the given tree metric, the cost of the derived edit path is computed
 * w.r.t. the given edit costs. 
 * 
 * @author Nils Kriege
 *
 * @param <V>
 * @param <E>
 */
public class GraphEditDistanceAssignmentLinearTreeDistance<V,E> implements GraphEditDistanceUpperBound<V,E>,Assignmentbased<V,E> {
	
	GraphEditCosts<V,E> graphEditCosts;
	TreeDistanceAssignmentSolver<Vertex> solver;

	public GraphEditDistanceAssignmentLinearTreeDistance(GraphEditCosts<V,E> graphEditCosts, TreeDistance<Vertex> assignmentCosts) {
		this.graphEditCosts = graphEditCosts;
//		this.solver = new TreeDistanceAssignmentSolver<Vertex>(assignmentCosts, new TreeDistanceAssignmentSolver.AmenableElementAssignment());
		this.solver = new TreeDistanceAssignmentSolver<Vertex>(assignmentCosts);
	}

	@Override
	public double compute(LGraph<V,E> lg1, LGraph<V,E> lg2) {
		return computeEditPath(lg1,lg2).getFirst();
	}
	
	private Pair<Double, int[]> computeEditPath(LGraph<V,E> lg1, LGraph<V,E> lg2)
	{
		int n = lg1.getGraph().getVertexCount();
		int m = lg2.getGraph().getVertexCount();
		ArrayList<Vertex> A = new ArrayList<Vertex>(n+m);
		ArrayList<Vertex> B = new ArrayList<Vertex>(n+m);
		for (Vertex v : lg1.getGraph().vertices()) {
			A.add(v);
		}
		for (Vertex v : lg2.getGraph().vertices()) {
			B.add(v);
		}
		// add dummy vertices for insertion and deletion
		for (int i=0; i<m; i++) {
			A.add(GraphEditAssignmentCostsTreeDistance.DELETION_DUMMY);
		}
		for (int i=0; i<n; i++) {
			B.add(GraphEditAssignmentCostsTreeDistance.INSERTION_DUMMY);
		}

		int[] assignment = solver.solve(A, B);
		double cost = GraphEditDistanceAssignment.editCosts(lg1, lg2, graphEditCosts, assignment);
		return new Pair<>(cost, assignment);
	}

	@Override
	public int[] getAssignment(LGraph<V, E> g1, LGraph<V, E> g2) {
		return computeEditPath(g1,g2).getSecond();
	}
	
	
	
}
