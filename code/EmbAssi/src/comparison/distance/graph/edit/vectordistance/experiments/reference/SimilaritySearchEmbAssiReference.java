package comparison.distance.graph.edit.vectordistance.experiments.reference;
import java.util.Collection;
import java.util.LinkedList;

import comparison.distance.graph.edit.GraphEditDistanceAssignment;
import comparison.distance.graph.edit.bounds.branch.Branch;
import comparison.distance.graph.edit.bounds.exact.GraphEditDistanceVerifier;
import comparison.distance.graph.edit.costs.GraphEditCosts;
import datastructure.Pair;
import graph.LGraph;

/**
 * Implements branch for similarity search
 * 
 * @author bause
 *
 * @param <V>
 * @param <E>
 */
public abstract class SimilaritySearchEmbAssiReference<V,E> {
	
	private GraphEditCosts<V, E> graphEditCosts;
	
	public abstract Collection<LGraph<V, E>> search(LGraph<V, E> query, double maxDist,Collection<LGraph<V, E>> database);
	
	public GraphEditCosts<V,E> getGraphEditCosts() {
		return graphEditCosts;
	}
}
