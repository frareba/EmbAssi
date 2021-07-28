package comparison.distance.graph.edit.vectordistance.dataconnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import datastructure.SparseFeatureVector;
import elki.data.SparseDoubleVector;
import elki.data.type.SimpleTypeInformation;
import elki.data.type.VectorFieldTypeInformation;
import elki.database.ids.DBIDFactory;
import elki.datasource.DatabaseConnection;
import elki.datasource.bundle.MultipleObjectsBundle;
import graph.LGraph;
import graph.attributes.Attributes;



/**
 * @author bause
 * Used to load vector data and corresponding graph into a database
 */
public class SparseVectorDataConnection implements DatabaseConnection {
	
	public static final SimpleTypeInformation<LGraph<Attributes, Attributes>> GRAPH_TYPE = new SimpleTypeInformation<>(LGraph.class);
	
	private int dim;
	private ArrayList<? extends LGraph<Attributes, Attributes>> graphs;
	private HashMap<LGraph<Attributes, Attributes>,SparseFeatureVector<Integer>> vectors;
	
	/**
	 * @param graphs
	 * @param vectors
	 * @param maxEntry maximum entry, that is filled in any of the vectors in vectors
	 */
	public SparseVectorDataConnection(ArrayList<? extends LGraph<Attributes, Attributes>> graphs,HashMap<LGraph<Attributes, Attributes>,SparseFeatureVector<Integer>> vectors,int maxEntry) {
		super();
		this.dim = maxEntry;
		this.graphs = graphs;
		this.vectors = vectors;
	}
	
	@Override
	public MultipleObjectsBundle loadData() {
		int ngraphs = this.graphs.size(); // Number of graphs
	    MultipleObjectsBundle b = new MultipleObjectsBundle();
	    b.setDBIDs(DBIDFactory.FACTORY.generateStaticDBIDRange(0, ngraphs));
	    
	    List<SparseDoubleVector> vecs = new ArrayList<>(ngraphs);
	    List<LGraph<Attributes, Attributes>> graphs = new ArrayList<>(ngraphs);
	    
	    for(int i = 0; i < ngraphs; i++) {
	    	graphs.add(this.graphs.get(i));
	    	vecs.add(new SparseDoubleVector(getDoubleVec(graphs.get(i))));
	    }
	    
	    SimpleTypeInformation<SparseDoubleVector> type = new VectorFieldTypeInformation<>(SparseDoubleVector.FACTORY, dim, dim, SparseDoubleVector.FACTORY.getDefaultSerializer());
	    b.appendColumn(type, vecs);
		b.appendColumn(GRAPH_TYPE, graphs);
		return b;
	}

	/**
	 * @param graph
	 * @return a double vector corresponding to the sparse feature vector of graph
	 */
	private double[] getDoubleVec(LGraph<Attributes, Attributes> graph) {
		double[] d = new double[this.dim];
		SparseFeatureVector<Integer> vec = this.vectors.get(graph);
		for(int i = 0; i<d.length;i++)
		{
			d[i] = vec.getValue(i);
		}
		return d;
	}
}

