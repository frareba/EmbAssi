package comparison.distance.graph.edit.vectordistance.experiments.reference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import comparison.distance.graph.edit.GraphEditDistanceAssignment;
import comparison.distance.graph.edit.bounds.branch.Branch;
import comparison.distance.graph.edit.bounds.exact.GraphEditDistanceVerifier;
import comparison.distance.graph.edit.costs.GraphEditCosts;
import datastructure.Pair;
import datastructure.Triple;
import elki.database.ids.DBIDArrayIter;
import graph.LGraph;

/**
 * Implements branch for similarity search
 * 
 * @author bause
 *
 * @param <V>
 * @param <E>
 */
public class BranchSimilaritySearchEmbAssi<V,E> extends SimilaritySearchEmbAssiReference<V,E>{
	
	private GraphEditCosts<V, E> graphEditCosts;
	private Branch<V, E> branch;
	

	public BranchSimilaritySearchEmbAssi(GraphEditCosts<V,E> gec) {
		this.graphEditCosts = gec;
		this.branch = new Branch<V,E>(gec);
	}
	
	@Override
	public Collection<LGraph<V, E>> search(LGraph<V, E> query, double maxDist,Collection<LGraph<V, E>> database) {
		LinkedList<LGraph<V, E>> c = new LinkedList<>();

		for (LGraph<V, E> lg : database) {
			Pair<Double, int[]> result = branch.computeEditPath(query, lg);
			int[] editPath =  result.getSecond();
			double bound = result.getFirst();
			if (bound > maxDist) {
				continue;
			}
			bound = GraphEditDistanceAssignment.editCosts(query, lg, graphEditCosts, editPath);
			if (bound <= maxDist) {
				continue;
			}
			c.add(lg);
		}
		return c;
	}
	
	public GraphEditCosts<V,E> getGraphEditCosts() {
		return graphEditCosts;
	}

}
