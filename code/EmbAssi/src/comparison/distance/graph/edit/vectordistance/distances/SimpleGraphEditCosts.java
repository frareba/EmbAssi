package comparison.distance.graph.edit.vectordistance.distances;

import comparison.distance.IdentityDistance;
import comparison.distance.graph.edit.costs.GraphEditCosts;

/**
 * @author bause
 * A class for simple graph edit costs. The (vertex and edge) relabeling cost are set to constant values.
 * @param <V>
 * @param <E>
 */
public class SimpleGraphEditCosts<V,E> extends GraphEditCosts<V,E>{

	double edgeRelabelingCost;
	double vertexRelabelingCost;
	
	
	public SimpleGraphEditCosts() {
		this(1, 1, 1, 1, 1, 1);
	}
	
	public SimpleGraphEditCosts(double vertexDeletionCost, double vertexInsertionCost, double edgeDeletionCost, double edgeInsertionCost, 
		double edgeRelabelingCost, double vertexRelabelingCost) {
		super(vertexDeletionCost, vertexInsertionCost, edgeDeletionCost, edgeInsertionCost, new IdentityDistance(edgeRelabelingCost), new IdentityDistance(vertexRelabelingCost));
		this.edgeRelabelingCost = edgeRelabelingCost;
		this.vertexRelabelingCost = vertexRelabelingCost;
	}
	
	public double vertexRelabeling() {
		return vertexRelabelingCost;
	}
	
	public double edgeRelabeling() {
		return edgeRelabelingCost;
	}
}

