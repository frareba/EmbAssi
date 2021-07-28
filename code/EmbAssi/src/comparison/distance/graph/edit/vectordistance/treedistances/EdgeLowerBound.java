package comparison.distance.graph.edit.vectordistance.treedistances;

import java.util.ArrayList;
import java.util.HashMap;
import comparison.distance.graph.edit.vectordistance.distances.GraphEditCostsEdgeVectorDistance;
import comparison.distance.graph.edit.vectordistance.distances.SimpleGraphEditCosts;
import comparison.distance.tree.TreeDistance;
import datastructure.Triple;
import graph.AdjListRootedTree;
import graph.LGraph;
import graph.TreeTools;
import graph.AdjListGraph.AdjListVertex;
import graph.AdjListRootedTree.AdjListRTreeVertex;
import graph.Graph.Edge;
import graph.Graph.Vertex;
import graph.properties.EdgeArray;
import graph.properties.VertexArray;
/**
 * @author bause
 * EdgeLowerBound (ELB): A lower bound to the graph edit distance, based on a matching of the edges of the two graphs.
 * @param <V>
 * @param <E>
 */
public class EdgeLowerBound<V,E> extends TreeDistance<Edge>{

	private ArrayList<? extends LGraph<V,E>> lgs;
	private SimpleGraphEditCosts<V,E> graphEditCosts;
	private HashMap <Triple<V,V,E>, AdjListRTreeVertex> labelToNode;
	
	private HashMap<Edge, AdjListRTreeVertex> mapToNode;
	
	private int maxNeighbors;
	
	public EdgeLowerBound(ArrayList<LGraph<V,E>> lgs, SimpleGraphEditCosts<V,E> graphEditCosts)
	{
		//initialize
		this.rt = new AdjListRootedTree();
		this.graphEditCosts = graphEditCosts;
		this.lgs = lgs;
		
		this.mapToNode = new HashMap<Edge,AdjListRTreeVertex>();
		this.labelToNode = new HashMap<Triple<V,V,E>,AdjListRTreeVertex>();
		
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
	public AdjListRTreeVertex mapToNode(Edge e) {
		if(this.mapToNode.containsKey(e))
		{
			return this.mapToNode.get(e);
		}// return dummy if edge is unknown
		System.out.println("Unknown edge");
		return this.mapToNode.get(GraphEditCostsEdgeVectorDistance.INSERTION_DUMMY);
	}
	
	private void generateTree()
	{
		// create the tree
		rt.createRoot();
		
		//for each vertex label: 
		// create node
		for(LGraph<V, E> g: this.lgs)
		{
			EdgeArray<E> ea =g.getEdgeLabel();
			VertexArray<V> va = g.getVertexLabel();
			for(Edge e: g.getGraph().edges())
			{
				E elabel = ea.get(e);
				V v1label = va.get(e.getFirstVertex());
				V v2label = va.get(e.getSecondVertex());
				Triple<V,V,E> label1 = new Triple<V,V,E>(v1label,v2label,elabel);
				Triple<V,V,E> label2 = new Triple<V,V,E>(v2label,v1label,elabel);
				if(this.labelToNode.containsKey(label1))
				{
					//assign vertex to node (fill vertex sets)
					mapToNode.put(e, this.labelToNode.get(label1));
				}
				else if(this.labelToNode.containsKey(label2))
				{
					//assign vertex to node (fill vertex sets)
					mapToNode.put(e, this.labelToNode.get(label2));
				}
				else
				{
					//create new node in tree
					AdjListRTreeVertex newLabelV = this.rt.createChild(this.rt.getRoot());
					this.labelToNode.put(label1, newLabelV);
					//assign vertex to node (fill vertex sets)
					mapToNode.put(e, this.labelToNode.get(label1));
				}
			}
		}		
				
		// add dummy node
		AdjListRTreeVertex r = rt.getRoot();
		AdjListRTreeVertex dummy = rt.createChild(r);
		mapToNode.put(GraphEditCostsEdgeVectorDistance.INSERTION_DUMMY, dummy);

		depth = TreeTools.computeDepth(rt);
		weight = new VertexArray<>(rt);
		
		//weight is the minimum of w and w2 (vertex relabeling or edge relabeling)
		double w = this.graphEditCosts.vertexRelabeling()/(double) this.maxNeighbors;
		double w2 = this.graphEditCosts.edgeRelabeling()*0.5;
		if(w>w2)
		{
			w = w2;
		}
		for (AdjListVertex v : rt.vertices()) {
			
			weight.set(v, w);
		}
		
		weight.set(dummy,this.graphEditCosts.getEdgeDeletionCost()-w);
		
		//TODO: das hier stimmte doch irgendwie nicht.. Jetzt muss man das obere natuerlich noch testen..
//		for (AdjListVertex v : rt.vertices()) {
//			weight.set(v, (this.graphEditCosts.edgeRelabeling()*0.5)/(double) this.maxNeighbors);
//		}
//		weight.set(dummy,(this.graphEditCosts.edgeRelabeling()*0.5)/(double) this.maxNeighbors);
		
	}

}
