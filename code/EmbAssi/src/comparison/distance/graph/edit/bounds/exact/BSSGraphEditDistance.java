package comparison.distance.graph.edit.bounds.exact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import ac.bss_ged.BSEditDistanceJNI;
import algorithm.graph.isomorphism.labelrefinement.EdgeLabelConverter;
import algorithm.graph.isomorphism.labelrefinement.VertexLabelConverter;
import comparison.distance.graph.edit.bounds.GraphEditDistanceExact;
import datastructure.Pair;
import graph.Graph;
import graph.Graph.Edge;
import graph.Graph.Vertex;
import graph.LGraph;
import graph.properties.EdgeArray;
import graph.properties.VertexArray;

/**
 * Java Interface to C++ native implementation of BSS-GED.
 * 
 * Xiaoyang Chen, Hongwei Huo, Jun Huan, and Jeffrey S. Vitter. 
 * An Efficient Algorithm for Graph Edit Distance Computation. Knowledge-Based Systems, 2019, 163: 762–775.
 * 
 * @author kriege
 *
 * @param <V>
 * @param <E>
 */
public class BSSGraphEditDistance<V,E> implements GraphEditDistanceExact<V, E>, GraphEditDistanceVerifier<V,E> {
	
	int width;
	
	/**
	 * @param width of the beam search
	 */
	public BSSGraphEditDistance(int width) {
		this.width = width;
	}
	
	@Override
	public double compute(LGraph<V, E> q, LGraph<V, E> g) {
		return compute(q, g, Double.MAX_VALUE);
	}
	
	@Override
	public double compute(LGraph<V, E> q, LGraph<V, E> g, double bound) {
		
		// floor is allowed due to the uniform cost-model
		int intBound = (int)bound; 
		int ub = Math.max(q.getGraph().getVertexCount(),g.getGraph().getVertexCount()) + 
				q.getGraph().getEdgeCount() + g.getGraph().getEdgeCount();
		intBound = Math.min(intBound, ub);
		
		// convert to integer labels
		VertexLabelConverter<E> vc = new VertexLabelConverter<E>(0);
		EdgeLabelConverter<Integer> ec = new EdgeLabelConverter<Integer>(0);
		LGraph<Integer,Integer> lg1 = ec.refineGraph(vc.refineGraph(q));
		LGraph<Integer,Integer> lg2 = ec.refineGraph(vc.refineGraph(g));
		
		Pair<int[], int[][]> Q = graphToPrimitiveDFS(lg1);
		Pair<int[], int[][]> G = graphToPrimitiveDFS(lg2);

		return compute(Q, G, intBound);
	}
	
	private double compute(Pair<int[], int[][]> Q, Pair<int[], int[][]> G, int bound) {
		
		BSEditDistanceJNI bsed = new BSEditDistanceJNI();
		int result = bsed.getEditDistance(width, Q.getFirst(), Q.getSecond(), G.getFirst(), G.getSecond(), bound);
		
		return result;
	}
	
	@Override
	public Collection<LGraph<V, E>> compute(LGraph<V, E> q, Collection<LGraph<V, E>> ds, double bound) {
		
		// floor is allowed due to the uniform cost-model
		int intBound = (int)bound; 
		
		// convert to integer labels
		VertexLabelConverter<E> vc = new VertexLabelConverter<E>(0);
		EdgeLabelConverter<Integer> ec = new EdgeLabelConverter<Integer>(0);
		LGraph<Integer,Integer> lg1 = ec.refineGraph(vc.refineGraph(q));
		
		Pair<int[], int[][]> Q = graphToPrimitiveDFS(lg1);

		LinkedList<LGraph<V, E>> r = new LinkedList<>();
		for (LGraph<V, E> g : ds) {
			LGraph<Integer,Integer> lg2 = ec.refineGraph(vc.refineGraph(g));
			Pair<int[], int[][]> G = graphToPrimitiveDFS(lg2);
			if (compute(Q, G, intBound) != -1) r.add(g);
		}
		
		return r;
	}

	private static Pair<int[], int[][]> graphToPrimitiveDFS(LGraph<Integer, Integer> lg) {

		Graph G = lg.getGraph();
		VertexArray<Integer> rank = new VertexArray<Integer>(G);
		int count = 0;
		ArrayList<Vertex> sortedbyDegree = new ArrayList<Vertex>();
		for(Vertex v: G.vertices())
		{
			sortedbyDegree.add(v);
		}
		
		Collections.sort(sortedbyDegree,new Comparator<Vertex>(){
	    public int compare(Vertex v1, Vertex v2) {
	        return v1.getDegree()-v2.getDegree();}});
		
		for (Vertex v : sortedbyDegree) {
			if (rank.get(v) == null) { // unvisited
				count = doDFS(G, rank, v, count);
			} 
		}
		
		//System.out.println(rank);

		
		VertexArray<Integer> va = lg.getVertexLabel();
		EdgeArray<Integer> ea = lg.getEdgeLabel();
		
		int[] vertices = new int[G.getVertexCount()];
		int[][] edges = new int[G.getEdgeCount()][];
		
		for (Vertex v : sortedbyDegree) {
			vertices[rank.get(v)] = va.get(v);
		}
		
		for (Edge e : G.edges()) {
			int iU = rank.get(e.getFirstVertex());
			int iV = rank.get(e.getSecondVertex());
			int l = ea.get(e);
			edges[e.getIndex()]   = new int[] {iU,iV,l};
		}

		
		return new Pair<int[], int[][]>(vertices, edges);
	}
	
	private static int doDFS(Graph G, VertexArray<Integer> rank, Vertex v, int count) {
		rank.set(v, count++);
		for (Vertex w : v.neighbors()) {
			if (rank.get(w) == null) {// unvisited
				count = doDFS(G, rank, w, count);
			}
		}
		return count;
	}

}
