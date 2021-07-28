package algorithm.graph.isomorphism.labelrefinement;

import graph.LGraph;
import graph.properties.EdgeArray;

/**
 * Base class for algorithms computing a edge label refinement.
 * 
 * @author kriege
 *
 * @param <IE> edge label type of the input graph, which is preserved by
 * the refinement step 
 * @param <IG> labeled graph type which can be used for refinement
 * @param <OV> output type of the refined vertex labels
 */
// TODO harmonize with VertexInvariant and VertexDescriptor
public abstract class EdgeLabelRefiner<IV,IG extends LGraph<IV, ?>,OE> extends LabelRefiner<IG,LGraph<IV, OE>> {

	/**
	 * Computes the refined edge labeling.
	 * @param lg
	 * @return
	 */
	public abstract EdgeArray<OE> edgeRefine(IG lg);
	
	@Override
	public LGraph<IV, OE> refineGraph(IG lg) {
		return new LGraph<IV, OE>(lg.getGraph(), lg.getVertexLabel(), edgeRefine(lg));
	}

}
