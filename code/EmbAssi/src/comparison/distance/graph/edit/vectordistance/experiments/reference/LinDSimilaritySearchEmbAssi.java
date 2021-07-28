package comparison.distance.graph.edit.vectordistance.experiments.reference;
import java.util.Collection;
import java.util.LinkedList;

import benchmark.dataset.AttrDataset;
import comparison.distance.graph.edit.GraphEditDistanceAssignment;
import comparison.distance.graph.edit.bounds.branch.Branch;
import comparison.distance.graph.edit.bounds.exact.GraphEditDistanceVerifier;
import comparison.distance.graph.edit.costs.GraphEditCosts;
import comparison.distance.graph.edit.treedistance.GraphEditAssignmentCostsTreeDistanceDiscrete;
import comparison.distance.graph.edit.treedistance.GraphEditDistanceAssignmentLinearTreeDistance;
import comparison.distance.tree.TreeDistance;
import datastructure.Pair;
import graph.LGraph;
import graph.Graph.Vertex;
import graph.attributes.Attributes;

/**
 * Implements branch for similarity search
 * 
 * @author bause
 *
 * @param <V>
 * @param <E>
 */
public class LinDSimilaritySearchEmbAssi<V,E> extends SimilaritySearchEmbAssiReference<V,E>{
	
	private GraphEditCosts<V, E> graphEditCosts;
	private GraphEditDistanceAssignmentLinearTreeDistance<V, E> distance;
	

	public LinDSimilaritySearchEmbAssi(GraphEditCosts<V,E> gec, AttrDataset ds) {
		this.graphEditCosts = gec;
		TreeDistance<Vertex> tdi = new GraphEditAssignmentCostsTreeDistanceDiscrete(1,ds); //TODO: maybe more iterations
		this.distance = new GraphEditDistanceAssignmentLinearTreeDistance<V,E>(gec, tdi);
	}
	
	@Override
	public Collection<LGraph<V, E>> search(LGraph<V, E> query, double maxDist,Collection<LGraph<V, E>> database) {
		LinkedList<LGraph<V, E>> c = new LinkedList<>();

		for (LGraph<V, E> lg : database) {
			double result = distance.compute(query, lg);
			if (result > maxDist) {
				c.add(lg);
			}
		}
		return c;
	}
}
