/**
 * 
 */
package comparison.distance.graph.edit.bounds.beamsearch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import comparison.distance.graph.edit.costs.GraphEditCosts;
import graph.Graph.Edge;
import graph.Graph.Vertex;
import graph.LGraph;

/**
 * Node in the search tree for the optimal edit path in order to compute the graph edit distance.
 * Each Node corresponds to a partial edit path between the two graphs induced by the matching between the vertices.
 * 
 * @author riesen
 * Source: https://github.com/dan-zam/graph-matching-toolkit
 * 
 */
public class TreeNode<V,E> implements Comparable<TreeNode<V,E>> {

	/** nodes of g1 are mapped to...*/
	private int[] matching;

	/** nodes of g2 are mapped to...*/
	private int[] inverseMatching;
	
	/** the current cost of this partial solution*/
	private double cost;

	/** the cost function defines the cost of individual node/edge operations*/
	private GraphEditCosts<V,E> cf;

	/** the original graphs */
	private LGraph<V,E> originalGraph1;
	private LGraph<V,E> originalGraph2;
	
	/** the graphs where the processed nodes are removed */
	private ArrayList<Vertex> unusedVertices1;
	private ArrayList<Vertex> unusedVertices2;
	
	private int depth;
	
	/**
	 * Constructor for the initial empty solution
	 * @param g2 graph 2
	 * @param g1 graph 1
	 * @param cf costs of the graph edit operations
	 */
	public TreeNode(LGraph<V,E> g1, LGraph<V,E> g2, GraphEditCosts<V,E> cf) {
		ArrayList<Vertex> v1 = new ArrayList<Vertex>();
		for(Vertex v: g1.getGraph().vertices())
		{
			v1.add(v);
		}
		ArrayList<Vertex> v2 = new ArrayList<Vertex>();
		for(Vertex v: g2.getGraph().vertices())
		{
			v2.add(v);
		}
		
		this.unusedVertices1 = v1;
		this.unusedVertices2 = v2;
		this.originalGraph1 = g1;
		this.originalGraph2 = g2;
		this.cost = 0;
		this.depth = 0;
		this.cf = cf;
		this.matching = new int[g1.getGraph().getVertexCount()];
		this.inverseMatching = new int[g2.getGraph().getVertexCount()];
		for (int i = 0; i < this.matching.length; i++) {
			this.matching[i] = -1;
		}
		for (int i = 0; i < this.inverseMatching.length; i++) {
			this.inverseMatching[i] = -1;
		}
	}
	
	/**
	 * Copy constructor in order to generate successors 
	 * of treenode @param o
	 */
	@SuppressWarnings("unchecked")
	public TreeNode(TreeNode<V,E> o) {
		this.unusedVertices1 = (ArrayList<Vertex>) o.getUnusedVertices1().clone();
		this.unusedVertices2 = (ArrayList<Vertex>) o.getUnusedVertices2().clone();
		this.cost = o.getCost();
		this.cf = o.getCf();
		this.matching =o.matching.clone();
		this.inverseMatching = o.inverseMatching.clone();
		this.originalGraph1 = o.originalGraph1;
		this.originalGraph2 = o.originalGraph2;
	}


	

	/**
	 * @return a list of successors of this treenode (extended solutions to 
	 * *this* solution)
	 */
	public LinkedList<TreeNode<V,E>> generateSuccessors(double bound) {
		
		bound = (double) Math.round(bound * 100000) / 100000; 
		// list with successors
		LinkedList<TreeNode<V,E>> successors = new LinkedList<TreeNode<V,E>>();
		
		// all vertices of g2 are processed, the remaining vertices of g1 are deleted
		if (this.unusedVertices2.isEmpty()) {
			TreeNode<V,E> tn = new TreeNode<V,E>(this);
			int n = tn.unusedVertices1.size();
			int e = 0;
			Iterator<Vertex> nodeIter = tn.unusedVertices1.iterator();
			while (nodeIter.hasNext()) {
				Vertex node = nodeIter.next();
				int i = node.getIndex();
				// find number of edges adjacent to vertex i
				e += this.getNumberOfAdjacentEdges(tn.matching,this.originalGraph1,i);
				tn.matching[i] = -2; // -2 = deletion
			}
			//add costs for deleting the vertices and their adjacent edges
			tn.addCost(n * this.cf.vertexDeletion(null, this.originalGraph1)); 
			tn.addCost(e * this.cf.edgeDeletion(null, originalGraph1));
			tn.unusedVertices1.clear();
			double c = (double)Math.round(tn.getCost() * 100000) / 100000;
			if (c <= bound){
				successors.add(tn);
			}
		} else { // there are still vertices in g2 but no vertices in g1, the vertices of g2 are inserted
			if (this.unusedVertices1.isEmpty()) {
				TreeNode<V,E> tn = new TreeNode<V,E>(this);
				int n = tn.unusedVertices2.size();
				int e = 0;
				Iterator<Vertex> nodeIter = tn.unusedVertices2.iterator();
				while (nodeIter.hasNext()) {
					Vertex node = nodeIter.next();
					int i = node.getIndex();
					// find number of edges adjacent to node i
					e += this.getNumberOfAdjacentEdges(tn.inverseMatching,this.originalGraph2,i);
					tn.inverseMatching[i] = -2; // -2 = insertion
				}
				tn.addCost(n * this.cf.vertexInsertion(this.originalGraph1, null, this.originalGraph2)); // null: some vertex.. isnt used in graph edit costs
				tn.addCost(e * this.cf.edgeInsertion(originalGraph1, null, originalGraph2));
				tn.unusedVertices2.clear();
				double c = (double)Math.round(tn.getCost() * 100000) / 100000;
				if (c <= bound){
					successors.add(tn);
				}				
			} else { // there are vertices in both g1 and g2
				for (int i = 0; i < this.unusedVertices2.size(); i++) {
					TreeNode<V,E> tn = new TreeNode<V,E>(this);
					Vertex start = tn.unusedVertices1.remove(0);
					Vertex end = tn.unusedVertices2.remove(i);
					tn.addCost(this.cf.vertexRelabeling(start, this.originalGraph1, end, this.originalGraph2));  //substitution cost
					int startIndex = start.getIndex();
					int endIndex = end.getIndex();
					tn.matching[startIndex] = endIndex;
					tn.inverseMatching[endIndex] = startIndex;
					// edge processing
					this.processEdges(tn, start, end);
					double c = (double)Math.round(tn.getCost() * 100000) / 100000;
					if (c <= bound){
						successors.add(tn);
					}
				}
				// deletion of a vertex from g1 is also a valid successor
				TreeNode<V,E> tn = new TreeNode<V,E>(this);
				Vertex deleted = tn.unusedVertices1.remove(0); //only delete the most "promising" vertex
				int i = deleted.getIndex();
				tn.matching[i] = -2; // deletion
				tn.addCost(this.cf.vertexInsertion(this.originalGraph1, null, this.originalGraph2));
				// find number of edges adjacent to vertex i
				int e = this.getNumberOfAdjacentEdges(tn.matching, this.originalGraph1, i);
				tn.addCost(this.cf.edgeInsertion(originalGraph1, null, originalGraph2) * e);
				double c = (double)Math.round(tn.getCost() * 100000) / 100000;
				if (c <= bound){
					successors.add(tn);
				}
			}
		}
		return successors;
	}

	/**
	 * Updates the cost of the partial edit path induced by @param tn
	 * by processing the edges of the two vertices @param start and @param end (that have not been taken into account yet).
	 * 
	 * @param tn treenode 
	 * @param start vertex of g1
	 * @param end vertex of g2
	 */
	private void processEdges(TreeNode<V,E> tn, Vertex start, Vertex end) {
		//process all edges that are inserted or relabeled
		for(Edge edge : start.edges()) // all edges adjacent to start
		{
			int start2Index = edge.getOppositeVertex(start).getIndex(); // neighbor of start
			if(tn.matching[start2Index]!= -1)  // other end has been handled
			{
				int end2Index = tn.matching[start2Index]; //matching of starts neighbor
				
				if (end2Index >= 0) { //vertex is not deleted
					Vertex end2 = this.originalGraph2.getGraph().getVertex(end2Index);
					if(this.originalGraph2.getGraph().hasEdge(end, end2)) // edge is not deleted 
					{
						Edge edge2 = this.originalGraph2.getGraph().getEdge(end, end2);
						tn.addCost(this.cf.edgeRelabeling(edge, originalGraph1, edge2, this.originalGraph2));
					}
					else // edge is deleted
					{
						tn.addCost(this.cf.edgeDeletion(edge, originalGraph1));
					}
				}
				else //node is deleted, so edge has to be deleted
				{
					tn.addCost(this.cf.edgeDeletion(edge, originalGraph1));
				}
			}
		}
		
		//process all edges that might be inserted 
		for(Edge edge : end.edges()) // all edges adjacent to end
		{
			int end2Index = edge.getOppositeVertex(end).getIndex(); // neighbor of end
			if(tn.inverseMatching[end2Index]!= -1)  // other end has been handled
			{
				//start2 has to be an existing vertex,
				//since vertices are only inserted if all other vertices have been processed,
				//their edges are then processed in the same step
				
				int start2Index = tn.inverseMatching[end2Index]; //matching of ends neighbor
				Vertex start2 = this.originalGraph1.getGraph().getVertex(start2Index);
				if (!this.originalGraph1.getGraph().hasEdge(start, start2)) // edge is inserted
				{
					tn.addCost(this.cf.edgeInsertion(originalGraph1, edge, originalGraph2));
				}
				// costs for relabeling edges are already counted 
			}
		}
	}

	/**
	 * @param graph 
	 * @return number of adjacent edges of vertex with index @param i
	 * NOTE: only edges (i,j) are counted if
	 * j-th vertex has been processed (deleted or substituted)
	 */
	private int getNumberOfAdjacentEdges(int[] m, LGraph<V, E> graph, int i) {
		int e= 0;
		Vertex v = graph.getGraph().getVertex(i);
		for(Edge edge : v.edges())
		{
			int j = edge.getOppositeVertex(v).getIndex();
			if (m[j]!=-1){ // count edges only if other end has been processed
				e += 1;
			}
		}
		return e;
	}

	/**
	 * adds @param c to the current solution cost
	 */
	private void addCost(double c) {
		this.cost += c;
	}
	
	/**
	 * @return true if all vertices are used in the current solution
	 */
	public boolean allNodesUsed() {
		if (unusedVertices1.isEmpty() && unusedVertices2.isEmpty()) {
			return true;
		}
		return false;
	}
	
	/**
	 * some getters and setters
	 */
	
	public ArrayList<Vertex> getUnusedVertices1() {
		return unusedVertices1;
	}

	public ArrayList<Vertex> getUnusedVertices2() {
		return unusedVertices2;
	}
	
	public double getCost() {
		return this.cost;
	}

	public GraphEditCosts<V,E> getCf() {
		return cf;
	}

	/** 
	 * In the open list treenodes are ordered according to their past cost.
	 * NOTE THAT CURRENTLY NO HEURISTIC IS IMPLEMENTED FOR ESTIMATING THE FUTURE COSTS
	 */
	@Override
	public int compareTo(TreeNode other) {
		if (this.depth-other.getDepth()==0){
			if ((this.getCost() - other.getCost())<0){
				return -1;
			} 
			if ((this.getCost() - other.getCost())>0){
				return 1;
			} 
		}
		if (this.depth - other.getDepth()<0){
			return -1;
		} 
		if (this.depth - other.getDepth()>0){
			return 1;
		}
		// we implement the open list as a TreeSet which does not allow 
		// two equal objects. That is, if two treenodes have equal cost, only one 
		// of them would be added to open, which would not be desirable
		return 1;
	}

	private int getDepth() {
		return this.depth;
	}
	
	public int[] getMatching()
	{
		return this.matching;
	}

	

}
