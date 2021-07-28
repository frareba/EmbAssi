package comparison.distance.graph.edit.vectordistance.experiments.reference;
import java.util.Collection;
import java.util.LinkedList;

import comparison.distance.graph.edit.GraphEditDistanceAssignment;
import comparison.distance.graph.edit.GraphEditDistanceAssignmentValue;
import comparison.distance.graph.edit.bounds.exact.GraphEditDistanceVerifier;
import comparison.distance.graph.edit.bounds.refinement.SimpleLocalSearch;
import comparison.distance.graph.edit.costs.GraphEditCosts;
import comparison.distance.graph.edit.search.cstar.StarEditDistance;
import datastructure.Pair;
import graph.GraphTools;
import graph.LGraph;

/**
 * Implements a variant of the graph edit distance range search algorithm described in the paper 
 * 
 * Zhiping Zeng, Anthony K. H. Tung, Jianyong Wang, Jianhua Feng, Lizhu Zhou:
 * Comparing Stars: On Approximating Graph Edit Distance. PVLDB 2(1): 25-36 (2009)
 * 
 * The approach assumes uniform costs for all edit operations and only discrete vertex/edge labels
 * In contrast to the normal CStar, the candidates are not verified in range search.
 * 
 * @author bause
 *
 * @param <V>
 * @param <E>
 */
public class CStarSimilaritySearchVariantEmbAssi<V,E> extends SimilaritySearchEmbAssiReference<V,E>{
	
	private GraphEditDistanceAssignmentValue<V,E> gedAssign;
	private SimpleLocalSearch<V, E> refinement;
	private GraphEditCosts<V, E> graphEditCosts;
	
	public CStarSimilaritySearchVariantEmbAssi(GraphEditCosts<V,E> gec) {
		this.gedAssign = new GraphEditDistanceAssignmentValue<>(new StarEditDistance<>());
		this.refinement = new SimpleLocalSearch<>();
		this.graphEditCosts = gec;
	}
	
	@Override
	public Collection<LGraph<V, E>> search(LGraph<V, E> query, double maxDist,Collection<LGraph<V, E>> database) {
		LinkedList<LGraph<V, E>> c = new LinkedList<>();

		for (LGraph<V, E> lg : database) {
			Pair<Double, int[]> p = gedAssign.computeEditPath(query, lg);
			double bound = p.getFirst();
			int[] editPath = p.getSecond();
			// make lower bound
			int maxDeg1 = GraphTools.maximumDegree(query.getGraph());
			int maxDeg2 = GraphTools.maximumDegree(lg.getGraph());
			bound /= Math.max(4, Math.max(maxDeg1, maxDeg2)+1);
			if (bound > maxDist) {
				continue;
			}
			bound = GraphEditDistanceAssignment.editCosts(query, lg, graphEditCosts, editPath);
			if (bound <= maxDist) {
				continue;
			}
			bound = refinement.refinedCost(query, lg, graphEditCosts, editPath);
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
