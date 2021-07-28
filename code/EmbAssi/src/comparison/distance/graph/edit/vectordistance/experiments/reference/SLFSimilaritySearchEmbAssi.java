package comparison.distance.graph.edit.vectordistance.experiments.reference;
import java.util.Collection;
import java.util.LinkedList;

import comparison.distance.graph.edit.GraphEditDistanceAssignment;
import comparison.distance.graph.edit.bounds.LabelCount;
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
public class SLFSimilaritySearchEmbAssi<V,E> extends SimilaritySearchEmbAssiReference<V,E>{
	
	private GraphEditCosts<V, E> graphEditCosts;
	private LabelCount<V, E> lc;
	

	public SLFSimilaritySearchEmbAssi(GraphEditCosts<V,E> gec) {
		this.graphEditCosts = gec;
		this.lc = new LabelCount<V, E>(gec);
	}
	
	public Collection<LGraph<V, E>> search(LGraph<V, E> query, double maxDist,Collection<LGraph<V, E>> database) {
		LinkedList<LGraph<V, E>> c = new LinkedList<>();

		for (LGraph<V, E> lg : database) {
			double result = lc.compute(query, lg);
			if (result <= maxDist) {
				c.add(lg);
			}
		}
		return c;
	}
	
	public GraphEditCosts<V,E> getGraphEditCosts() {
		return graphEditCosts;
	}

}
