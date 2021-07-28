package comparison.distance.graph.edit.vectordistance.treedistances;

import java.util.ArrayList;
import java.util.HashMap;
import comparison.distance.graph.edit.treedistance.GraphEditAssignmentCostsTreeDistance;
import comparison.distance.graph.edit.vectordistance.distances.SimpleGraphEditCosts;
import comparison.distance.tree.TreeDistance;
import graph.AdjListRootedTree;
import graph.LGraph;
import graph.TreeTools;
import graph.AdjListGraph.AdjListVertex;
import graph.AdjListRootedTree.AdjListRTreeVertex;
import graph.Graph.Vertex;
import graph.properties.VertexArray;

/**
 * @author bause
 * LabelLowerBound (LLB): A lower bound to the graph edit distance, based on the minimal amount of vertex-relabeling needed.
 * @param <V>
 * @param <E>
 */
public class LabelLowerBound<V,E> extends TreeDistance<Vertex>{
	
	private ArrayList<? extends LGraph<V,E>> lgs;
	private SimpleGraphEditCosts<V,E> graphEditCosts;
	private HashMap <V, AdjListRTreeVertex> labelToNode;
	
	private HashMap<Vertex, AdjListRTreeVertex> mapToNode;
	
	public LabelLowerBound(ArrayList<LGraph<V,E>> lgs, SimpleGraphEditCosts<V,E> graphEditCosts)
	{
		//initialize
		this.rt = new AdjListRootedTree();
		this.graphEditCosts = graphEditCosts;
		this.lgs = lgs;
		
		this.mapToNode = new HashMap<Vertex,AdjListRTreeVertex>();
		this.labelToNode = new HashMap<V,AdjListRTreeVertex>();
		
		this.generateTree();
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
		
		//for each vertex label: 
		// create node
		for(LGraph<V, E> g: this.lgs)
		{
			VertexArray<V> va =g.getVertexLabel();
			for(Vertex v: g.getGraph().vertices())
			{
				V label = va.get(v);
				if(!this.labelToNode.containsKey(label))
				{
					//create new node in tree
					AdjListRTreeVertex newLabelV = this.rt.createChild(this.rt.getRoot());
					
					this.labelToNode.put(label, newLabelV);
				}
				
				//assign vertex to node (fill vertex sets)
				mapToNode.put(v, this.labelToNode.get(label));
			}
		}		
				
		// add dummy node
		AdjListRTreeVertex r = rt.getRoot();
		AdjListRTreeVertex dummy = rt.createChild(r);
		mapToNode.put(GraphEditAssignmentCostsTreeDistance.INSERTION_DUMMY, dummy);

		depth = TreeTools.computeDepth(rt);
		weight = new VertexArray<>(rt);
		for (AdjListVertex v : rt.vertices()) {
			weight.set(v, this.graphEditCosts.vertexRelabeling()*0.5); 
		}
		weight.set(dummy, this.graphEditCosts.vertexInsertion(null, null, null)-this.graphEditCosts.vertexRelabeling()*0.5); 
	}
}
