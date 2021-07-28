package comparison.distance.graph.edit.search.cstar;

import java.util.Map.Entry;

import comparison.distance.graph.edit.costs.GraphEditAssignmentCosts;
import datastructure.SparseFeatureVector;
import graph.Graph.Vertex;
import graph.properties.VertexArray;
import graph.LGraph;

/**
 * Defines the Star Edit Distance according to Lemma 4.1
 * of the paper Zeng et al. "Comparing Stars", VLDB 2009.
 * @author kriege
 *
 * @param <V>
 * @param <E>
 */
public class StarEditDistance<V,E> implements GraphEditAssignmentCosts<V,E> {

	@Override
	public double vertexSubstitution(Vertex vEdit, LGraph<V,E> lgEdit, Vertex vTarget, LGraph<V,E> lgTarget) {

		V l1 = lgEdit.getVertexLabel().get(vEdit);
		V l2 = lgTarget.getVertexLabel().get(vTarget);
		double T = l1.equals(l2) ? 0 : 1;
		double d = Math.abs(vEdit.getDegree() - vTarget.getDegree());
		double M = Math.max(vEdit.getDegree(), vTarget.getDegree());
		// compute multiset intersection
		SparseFeatureVector<V> vl1 = createVertexLabelVector(vEdit, lgEdit);
		SparseFeatureVector<V> vl2 = createVertexLabelVector(vTarget, lgTarget);
		for (Entry<V, Double> vl1e  : vl1.nonZeroEntries()) {
			M -= Math.min(vl1e.getValue(), vl2.getValue(vl1e.getKey()));
		}
		return T+d+M;
	}
	
	private SparseFeatureVector<V> createVertexLabelVector(Vertex v, LGraph<V, E> lg) {
		VertexArray<V> va = lg.getVertexLabel();
		
		SparseFeatureVector<V> r = new SparseFeatureVector<>();
		for (Vertex u : v.neighbors()) {
			r.increaseByOne(va.get(u));
		}
		
		return r;
	}

	@Override
	public double vertexDeletion(Vertex v, LGraph<V, E> lg) {
		double T = 1;
		double d = v.getDegree();
		double M = v.getDegree();
		return T+d+M;
	}

	@Override
	public double vertexInsertion(LGraph<V, E> lgEdit, Vertex vTarget, LGraph<V, E> lgTarget) {
		double T = 1;
		double d = vTarget.getDegree();
		double M = vTarget.getDegree();
		return T+d+M;
	}
	

}
