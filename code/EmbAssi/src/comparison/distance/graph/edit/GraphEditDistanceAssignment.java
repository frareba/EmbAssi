package comparison.distance.graph.edit;

import algorithm.assignment.AssignmentSolver;
import comparison.distance.graph.edit.bounds.GraphEditDistanceUpperBound;
import comparison.distance.graph.edit.costs.GraphEditAssignmentCosts;
import comparison.distance.graph.edit.costs.GraphEditAssignmentCostsExtended;
import comparison.distance.graph.edit.costs.GraphEditCosts;
import graph.Graph;
import graph.Graph.Edge;
import graph.Graph.Vertex;
import graph.LGraph;

/**
 * Determines a vertex mapping between two graphs based on an optimal
 * assignment between their vertices (and dummy vertices for vertex and edge 
 * deletion). The mapping is then used to derive a sequence of edit operations
 * and its cost is returned as the graph edit distance.
 * 
 * @author Nils Kriege
 *
 * @param <V>
 * @param <E>
 */
public class GraphEditDistanceAssignment<V,E> extends GraphEditDistanceAssignmentValue<V, E> implements GraphEditDistanceUpperBound<V, E> {

	GraphEditCosts<V,E> graphEditCosts;
	
	public GraphEditDistanceAssignment() {
		this(new AssignmentSolver.Hungarian(), new GraphEditCosts<>(), new GraphEditAssignmentCostsExtended<V,E>(new GraphEditCosts<>()));

	}
	
	/**
	 * @param graphEditCosts the costs for graph edit operations
	 * @param assignmentCosts the costs for computing the vertex assignment,
	 * use {@see GraphEditAssignmentCostsExtended} in order to take graph 
	 * structure into account!
	 */
	public GraphEditDistanceAssignment(GraphEditCosts<V,E> graphEditCosts, GraphEditAssignmentCosts<V, E> assignmentCosts) {
		this(new AssignmentSolver.Hungarian(), graphEditCosts, assignmentCosts);
	}

	/**
	 * Allows to specify the assignment solver.
	 * @param solver the assignment solver
	 * @see GraphEditDistanceAssignment#GraphEditDistanceAssignment(GraphEditCosts, GraphEditAssignmentCosts)
	 */
	public GraphEditDistanceAssignment(AssignmentSolver solver, GraphEditCosts<V,E> graphEditCosts, GraphEditAssignmentCosts<V, E> assignmentCosts) {
		super(solver, assignmentCosts);
		this.graphEditCosts = graphEditCosts;
	}
	
	@Override
	public double compute(LGraph<V, E> lg1, LGraph<V, E> lg2) {
		double[][] C = costMatrix(lg1, lg2);
		int[] assignment = solver.solve(C);
		
		return editCosts(lg1, lg2, graphEditCosts, assignment);
	}
	
	public GraphEditCosts<V,E> getGraphEditCosts() {
		return graphEditCosts;
	}
	
	/**
	 * @param lgEdit start graph
	 * @param lgTarget target graph
	 * @param graphEditCosts the costs for graph edit operations
	 * @param assignment mapping between the vertices of the two graphs 
	 * @return the edit costs derived from the assignment
	 */
	public static <V,E> double editCosts(LGraph<V, E> lgEdit, LGraph<V, E> lgTarget, GraphEditCosts<V,E> graphEditCosts, int[] assignment) {
		
		Graph gEdit = lgEdit.getGraph();
		Graph gTarget = lgTarget.getGraph();
		int n = gEdit.getVertexCount();
		int m = gTarget.getVertexCount();
		
		double r = 0;
		
		
		// vertex deletion and relabeling
		for (Vertex vEdit : gEdit.vertices()) {
			int vTargetIndex = assignment[vEdit.getIndex()];
			if (vTargetIndex >= m) {
				// vertex deleted
				r += graphEditCosts.vertexDeletion(vEdit, lgEdit);
			} else {
				// vertex preserved, relabeling possible
				Vertex vTarget = gTarget.getVertex(vTargetIndex);
				r += graphEditCosts.vertexRelabeling(vEdit, lgEdit, vTarget, lgTarget);
			}			
		}
		
		// vertex insertions
		for (Vertex v : gTarget.vertices()) {
			// lower left corner
			if (assignment[v.getIndex()+n] < m) {
				r += graphEditCosts.vertexInsertion(lgEdit, v, lgTarget);
			}
		}
		
		// edge relabeling and deletion
		boolean[] matchedEdges = new boolean[gTarget.getEdgeCount()];
		for (Edge eEdit : gEdit.edges()) {
			int uTargetIndex = assignment[eEdit.getFirstVertex().getIndex()];
			int vTargetIndex = assignment[eEdit.getSecondVertex().getIndex()];
			Edge eTarget = null;
			if (uTargetIndex < m && vTargetIndex < m) { 
				Vertex uTarget = gTarget.getVertex(uTargetIndex);
				Vertex vTarget = gTarget.getVertex(vTargetIndex);
				eTarget = gTarget.getEdge(uTarget, vTarget);
			}
			if (eTarget != null) {
				// both vertices are preserved and edge exists;
				// relabeling possible
				r += graphEditCosts.edgeRelabeling(eEdit, lgEdit, eTarget, lgTarget);
				matchedEdges[eTarget.getIndex()] = true;
			} else {
				// deletion
				r += graphEditCosts.edgeDeletion(eEdit, lgEdit);
			}
		}
		
		// edge insertion
		for (Edge eTarget : gTarget.edges()) {
			if (!matchedEdges[eTarget.getIndex()]) {
				r += graphEditCosts.edgeInsertion(lgEdit, eTarget, lgTarget);
			}
		}
		
		return r;
	}
	

}
