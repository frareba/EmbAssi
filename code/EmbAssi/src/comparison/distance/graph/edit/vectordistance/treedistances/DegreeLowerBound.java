package comparison.distance.graph.edit.vectordistance.treedistances;

import java.util.ArrayList;
import java.util.HashMap;

import comparison.distance.graph.edit.treedistance.GraphEditAssignmentCostsTreeDistance;
import comparison.distance.graph.edit.vectordistance.distances.SimpleGraphEditCosts;
import comparison.distance.tree.TreeDistance;
import graph.Graph.Vertex;
import graph.properties.VertexArray;
import graph.AdjListRootedTree;
import graph.LGraph;
import graph.TreeTools;
import graph.AdjListGraph.AdjListVertex;
import graph.AdjListRootedTree.AdjListRTreeVertex;
/**
 * @author bause
 * DegreeLowerBound (DLB): A lower bound to the graph edit distance, based on the minimal amount of edge-insertion/deletion needed.
 * @param <V>
 * @param <E>
 */
public class DegreeLowerBound<V,E> extends TreeDistance<Vertex> {
	
	private ArrayList<? extends LGraph<V,E>> lgs;
	private SimpleGraphEditCosts<V,E> graphEditCosts;
	
	HashMap<Vertex, AdjListRTreeVertex> mapToNode;
	private HashMap <Integer, AdjListRTreeVertex> degreeToNode;
	
	private int maxNeighbors;
	
	public DegreeLowerBound(ArrayList<LGraph<V,E>> lgs, SimpleGraphEditCosts<V,E> graphEditCosts)
	{
		//initialize
		this.rt = new AdjListRootedTree();
		this.graphEditCosts = graphEditCosts;
		this.lgs = lgs;
		
		this.mapToNode = new HashMap<Vertex,AdjListRTreeVertex>();
		this.degreeToNode = new HashMap<Integer,AdjListRTreeVertex>();
		
		this.computeMaxNeighbors();
		
		this.generateTree();
	}

	private void computeMaxNeighbors() {
		for(LGraph<V, E> g: this.lgs)
		{
			for(Vertex v: g.getGraph().vertices())
			{
				if(v.getDegree()>this.maxNeighbors)
				{
					this.maxNeighbors = v.getDegree();
				}
			}
		}
	}

	@Override
	public AdjListRTreeVertex mapToNode(Vertex t) {
		if(this.mapToNode.containsKey(t))
		{
			return this.mapToNode.get(t);
		}// return dummy if vertex is unknown
		System.out.println("Unknown vertex");
		return this.mapToNode.get(GraphEditAssignmentCostsTreeDistance.INSERTION_DUMMY);
	}
	
	private void generateTree()
	{
		// create the tree
		rt.createRoot();
		this.degreeToNode.put(0, rt.getRoot());
		
		//for each degree
		for(int i = 1; i<= this.maxNeighbors; i++)
		{
			//create new node in tree
			AdjListRTreeVertex newLabelV = this.rt.createChild(this.degreeToNode.get(i-1));
			this.degreeToNode.put(i, newLabelV);

		}

		// add dummy node
		AdjListRTreeVertex r = rt.getRoot();
		AdjListRTreeVertex dummy = rt.createChild(r);
		mapToNode.put(GraphEditAssignmentCostsTreeDistance.INSERTION_DUMMY, dummy);

		depth = TreeTools.computeDepth(rt);
		weight = new VertexArray<>(rt);
		for (AdjListVertex v : rt.vertices()) {
			weight.set(v, this.graphEditCosts.edgeDeletion(null, null) * 0.5);
		}
		weight.set(dummy, 0.0);
		
		// fill vertex sets
		for (LGraph<V, E> g : this.lgs) {
			for (Vertex v : g.getGraph().vertices()) {
				// assign vertex to node
				mapToNode.put(v, this.degreeToNode.get(v.getDegree()));
			}
		}

	}
}