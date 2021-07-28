package comparison.distance.graph.edit;

import algorithm.assignment.AssignmentSolver;
import comparison.distance.Distance;
import comparison.distance.graph.edit.bounds.refinement.Assignmentbased;
import comparison.distance.graph.edit.costs.GraphEditAssignmentCosts;
import datastructure.Pair;
import graph.Graph;
import graph.LGraph;
import graph.Graph.Vertex;

public class GraphEditDistanceAssignmentValue<V,E> implements Distance<LGraph<V,E>>,Assignmentbased<V,E>  {
	
	protected AssignmentSolver solver;
	GraphEditAssignmentCosts<V,E> assignmentCosts;
		
	public GraphEditDistanceAssignmentValue(GraphEditAssignmentCosts<V,E> assignmentCosts) {
		this.solver = new AssignmentSolver.Hungarian();
		this.assignmentCosts = assignmentCosts;
	}
	
	public GraphEditDistanceAssignmentValue(AssignmentSolver solver, GraphEditAssignmentCosts<V,E> assignmentCosts) {
		this.solver = solver;
		this.assignmentCosts = assignmentCosts;
	}
	
	@Override
	public double compute(LGraph<V, E> lg1, LGraph<V, E> lg2) {
		double[][] C = costMatrix(lg1, lg2);
		return solver.minimumCost(C);
	}
	
	public Pair<Double, int[]> computeEditPath(LGraph<V, E> lg1, LGraph<V, E> lg2) {
		double[][] C = costMatrix(lg1, lg2);
		int[] assignment = solver.solve(C);
		double cost = AssignmentSolver.assignmentCost(assignment, C);
		return new Pair<>(cost, assignment);
	}
	
	/**
	 * @param lgEdit start graph
	 * @param lgTarget target graph
	 * @return the cost matrix for the assignment between the vertices of the two graphs
	 */
	public double[][] costMatrix(LGraph<V, E> lgEdit, LGraph<V, E> lgTarget) {
		Graph gEdit = lgEdit.getGraph();
		Graph gTarget = lgTarget.getGraph();
		
		int n = gEdit.getVertexCount();
		int m = gTarget.getVertexCount();
		
		double[][] C = new double[n+m][n+m];
		
		// vertex matching (upper left corner)
		for (int i=0; i<n; i++) {
			for (int j=0; j<m; j++) {
				Vertex vEdit = gEdit.getVertex(i);
				Vertex vTarget = gTarget.getVertex(j);
				
				C[i][j] = assignmentCosts.vertexSubstitution(vEdit, lgEdit, vTarget, lgTarget);
			}
		}
		
		// vertex insertion (lower left corner)
		for (int i=n; i<n+m; i++) {
			for (int j=0; j<m; j++) {
				C[i][j] = Double.POSITIVE_INFINITY;
			}
		}
		for (int j=0; j<m; j++) {
			Vertex v2 = gTarget.getVertex(j);
			C[n+j][j] = assignmentCosts.vertexInsertion(lgEdit, v2, lgTarget);
		}
		
		// vertex deletion (upper right corner)
		for (int i=0; i<n; i++) {
			for (int j=m; j<n+m; j++) {
				C[i][j] = Double.POSITIVE_INFINITY;
			}
		}
		for (int i=0; i<n; i++) {
			Vertex v1 = gEdit.getVertex(i);
			C[i][m+i] = assignmentCosts.vertexDeletion(v1, lgEdit);
		}

		// zero fill-in (lower right corner)
		for (int i=n; i<n+m; i++) {
			for (int j=m; j<n+m; j++) {
				C[i][j] = 0;
			}
		}		
		
		return C;
	}

	@Override
	public int[] getAssignment(LGraph<V, E> g1, LGraph<V, E> g2) {
		return computeEditPath(g1,g2).getSecond();
	}
	

}
