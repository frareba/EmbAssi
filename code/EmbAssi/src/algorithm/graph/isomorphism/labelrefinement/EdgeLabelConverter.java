package algorithm.graph.isomorphism.labelrefinement;

import java.util.HashMap;

import graph.Graph;
import graph.Graph.Edge;
import graph.LGraph;
import graph.properties.EdgeArray;


/**
 * Converts the edge labels of a graph to integers in {0, ..., n}.
 * An object of this class can be used to subsequently process multiple
 * graphs, internally building a 1:1 mapping of edge labels occurring 
 * in the set of graphs and new integer labels. This map can be cleared
 * manually if required.
 * 
 * @see #clearLabelMap()
 * 
 * @author kriege
 * 
 * @param <IV> vertex label type of the input graph, which is preserved by 
 * the refinement step
 */
public class EdgeLabelConverter<IV> extends EdgeLabelRefiner<IV, LGraph<IV,?>, Integer> {

	private int offset;
	private HashMap<Object, Integer> labelMap;
	
	public EdgeLabelConverter() {
		this(0);
	}
	
	/**
	 * Creates a new vertex label converter with an initial empty mapping.
	 * @param offset the vertex label assign to the first unseen vertex.
	 */
	public EdgeLabelConverter(int offset) {
		labelMap = new HashMap<Object, Integer>();
		this.offset = offset;
	}
	
	@Override
	public EdgeArray<Integer> edgeRefine(LGraph<IV, ?> lg) {
		Graph g = lg.getGraph();
		EdgeArray<?> ea = lg.getEdgeLabel();
		
		EdgeArray<Integer> eaRefined = new EdgeArray<Integer>(g);
		for (Edge e : g.edges()) {
			Object label = ea.get(e);
			Integer newLabel = labelMap.get(label);
			if (newLabel == null) {
				newLabel = getNextLabel();
				labelMap.put(label, newLabel);
			}
			eaRefined.set(e, newLabel);		
		}
		
		return eaRefined;
	}
	
	/**
	 * Clears the current vertex label mapping.
	 */
	public void clearLabelMap() {
		offset = getNextLabel();
		labelMap.clear();
	}
	
	/**
	 * Returns the integer label that will be assigned to the next unseen 
	 * vertex label.
	 */
	public int getNextLabel() {
		return labelMap.size()+offset;
	}

}
