package comparison.distance.graph.edit.vectordistance.dataconnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import datastructure.SparseFeatureVector;
import elki.data.SparseIntegerVector;
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
public class IntegerVectorDataConnection implements DatabaseConnection {
	
	public static final SimpleTypeInformation<LGraph<Attributes, Attributes>> GRAPH_TYPE = new SimpleTypeInformation<>(LGraph.class);
	
	private int dim;
	private ArrayList<? extends LGraph<Attributes, Attributes>> graphs;
	private HashMap<LGraph<Attributes, Attributes>,SparseFeatureVector<Integer>> vectors;
	private double factor; 
	
	/**
	 * @param graphs
	 * @param vectors
	 * @param maxEntry maximum entry, that is filled in any of the vectors in vectors
	 */
	public IntegerVectorDataConnection(ArrayList<? extends LGraph<Attributes, Attributes>> graphs,HashMap<LGraph<Attributes, Attributes>,SparseFeatureVector<Integer>> vectors,int maxEntry) {
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
	    
	    
	    List<double[]> vecs = new ArrayList<>(ngraphs);
	    List<SparseIntegerVector> intVecs= new ArrayList<>(ngraphs);
	    List<LGraph<Attributes, Attributes>> graphs = new ArrayList<>(ngraphs);
	    
	    for(int i = 0; i < ngraphs; i++) {
	    	graphs.add(this.graphs.get(i));
	    	vecs.add(getDoubleVec(graphs.get(i)));
	    }
	    
	    setFactor(vecs);
	    
	    for(int i = 0; i < ngraphs; i++) {
	    	intVecs.add(new SparseIntegerVector(getIntVec(vecs.get(i))));
	    }
	    
	    SimpleTypeInformation<SparseIntegerVector> type = new VectorFieldTypeInformation<>(SparseIntegerVector.FACTORY, dim, dim, SparseIntegerVector.FACTORY.getDefaultSerializer());
	    b.appendColumn(type, intVecs);
	    b.appendColumn(GRAPH_TYPE, graphs);
		return b;
	}

	private void setFactor(List<double[]> vecs) {
		double f = 100; //TODO: What if there is an entry with more than two decimal places?
		this.factor = f;
	}

	/**
	 * generates an integer vector from a double vector by multiplying every entry with the specified factor
	 * @param doubleVector 
	 * @return a vector of integers
	 */
	private int[] getIntVec(double[] doubleVector) {
		int[] intVector = new int[doubleVector.length];
		for(int i = 0; i< intVector.length; i++)
		{
			intVector[i] = (int) (doubleVector[i]*this.factor);
		}
		
		return intVector;
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
	/**
	 * @return the factor, which all vector entries have been multiplied with (to get the Manhattan distance of the original vectors, simply divide by this factor).
	 */
	public double getFactor()
	{
		return this.factor;
	}
}

