package comparison.distance.graph.edit.vectordistance.distances;

import java.util.ArrayList;
import java.util.LinkedList;
import comparison.distance.tree.TreeDistance;
import datastructure.SparseFeatureVector;
import graph.AdjListGraph.AdjListEdge;
import graph.AdjListRootedTree.AdjListRTreeVertex;
import graph.Graph.Vertex;
import graph.properties.VertexArray;

/**
 * @author bause / this code is mostly from class comparison.distance.tree.TreeDistanceAssignmentSolver
 * Class to compute the vectors of sets regarding a treedistance
 * @param <T>
 */
public class Vectors<T> {
	
	private TreeDistance<T> dist;
	private ArrayList<AdjListEdge> edges; // index of edge = index in vector
	
	/**
	 * @param dist distance to be used to compute the vectors
	 */
	public Vectors(TreeDistance<T> dist) {
		this.dist = dist;
		this.edges = new ArrayList<AdjListEdge>();
		for(AdjListEdge e:dist.getRootedTree().edges())
		{
			this.edges.add(e);
		}
		
	}
	
	/**
	 * @param set Set 
	 * @return the vector of set regarding the used distance
	 */
	public SparseFeatureVector<Integer> computeVector(ArrayList<T> set)
	{
		SparseFeatureVector<Integer> vector =new SparseFeatureVector<Integer>();
		
		VertexArray<ArrayList<Integer>> vertices = new VertexArray<>(this.dist.getRootedTree());
		VertexArray<Integer> degree = new VertexArray<>(this.dist.getRootedTree()); // the number of children with respect to the relevant subtree R
		VertexArray<Boolean> marked = new VertexArray<>(this.dist.getRootedTree()); // marks the vertices in R

		for (Vertex v : this.dist.getRootedTree().vertices()) {
			vertices.set(v, new ArrayList<>());
			degree.set(v, 0);
			marked.set(v, false);
		}
		
		int minDepth = Integer.MAX_VALUE;
		ArrayList<AdjListRTreeVertex> N = new ArrayList<>(); 
		
		// initialize
		for (int i=0; i<set.size(); i++) {
			AdjListRTreeVertex v = this.dist.mapToNode(set.get(i));
			if (!marked.get(v)) {
				N.add(v); // add v to R if not already present
				marked.set(v, true);
				minDepth = Math.min(minDepth, this.dist.getDepth(v));
			}
			vertices.get(v).add(i);
		}
		// mark relevant subtree, compute degree
		LinkedList<AdjListRTreeVertex> roots = new LinkedList<>();
		for (AdjListRTreeVertex v : N) {
			while (this.dist.getDepth(v) > minDepth) {
				v = v.getParent();
				degree.set(v, degree.get(v) + 1);
				if (marked.get(v))
					break;
				marked.set(v, true);
			}
			// root has degree 1 when it is found for the first time;
			// and degree 0 if it is in N
			if (this.dist.getDepth(v) == minDepth && degree.get(v) <= 1) {
				roots.add(v);
			}
		}
		while (roots.size() > 1) {
			LinkedList<AdjListRTreeVertex> parents = new LinkedList<>();
			for (AdjListRTreeVertex r : roots) {
				AdjListRTreeVertex p = r.getParent();
				if (!marked.get(p)) {
					parents.add(p);
					marked.set(p, true);
				}
				degree.set(p, degree.get(p)+1);
			}
			roots = parents;
			minDepth--;
		}
		if(roots.size() ==0)
		{
			roots.add(this.dist.getRootedTree().getRoot());
		}
		AdjListRTreeVertex root = roots.getFirst(); // the only remaining element	
		// find leaves
		LinkedList<AdjListRTreeVertex> leaves = new LinkedList<>();
		for (AdjListRTreeVertex v : N) {
			if (degree.get(v)==0) leaves.add(v);
		}
		while (!leaves.isEmpty()) {
			AdjListRTreeVertex l = leaves.poll();
			
			if(l == root) // all relevant edges have been visited
			{
				break;
			}
			ArrayList<Integer> eA = vertices.get(l);
			
			// compute the vector entry
			AdjListEdge edge = this.dist.getRootedTree().getEdge(l, l.getParent());

			int index = this.edges.indexOf(edge);
			double weight = this.dist.getWeight(l);

			vector.setValue(index, eA.size()*weight);
			
			// propagate all elements to parent
			if (l != root) {
				AdjListRTreeVertex p = l.getParent();
				vertices.get(p).addAll(eA);
				eA.clear();

				int newDegree = degree.get(p) - 1;
				degree.set(p, newDegree);
				if (newDegree == 0) leaves.add(p);
			}
			marked.set(l, false);

		}
		return vector;
	}

}
