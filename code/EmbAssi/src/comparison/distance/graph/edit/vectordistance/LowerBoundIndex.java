package comparison.distance.graph.edit.vectordistance;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import benchmark.dataset.AttrDataset;
import comparison.distance.Distance;
import comparison.distance.graph.edit.bounds.exact.GraphEditDistanceVerifier;
import comparison.distance.graph.edit.vectordistance.dataconnection.IntegerVectorDataConnection;
import comparison.distance.graph.edit.vectordistance.dataconnection.SparseVectorDataConnection;
import comparison.distance.graph.edit.vectordistance.distances.GraphEditCostsVectorDistance;
import datastructure.SparseFeatureVector;
import datastructure.Triple;
import elki.data.SparseNumberVector;
import elki.data.type.SimpleTypeInformation;
import elki.data.type.TypeUtil;
import elki.database.Database;
import elki.database.StaticArrayDatabase;
import elki.database.ids.DBIDArrayIter;
import elki.database.ids.DBIDRange;
import elki.database.ids.DBIDRef;
import elki.database.ids.DBIDUtil;
import elki.database.ids.DoubleDBIDIter;
import elki.database.ids.KNNHeap;
import elki.database.query.ExactPrioritySearcher;
import elki.database.query.PrioritySearcher;
import elki.database.query.QueryBuilder;
import elki.database.relation.Relation;
import elki.datasource.DatabaseConnection;
import elki.distance.minkowski.SparseManhattanDistance;
import elki.index.tree.metrical.covertree.CoverTree;
import elki.utilities.ELKIBuilder;
import graph.LGraph;
import graph.attributes.Attributes;

/**
* Class for getting candidates for similarity search quickly
* This class uses elki for the underlying database and search algorithm and a VectorDistance has to be provided.
* @author bause
*
*/
public class LowerBoundIndex{
	
	public static final SimpleTypeInformation<LGraph<Attributes, Attributes>> GRAPH_TYPE = new SimpleTypeInformation<>(LGraph.class);
	
	private AttrDataset ds;
	private GraphEditCostsVectorDistance vecDist;
	private boolean integerVec;
	private double distanceFactor;
	
	//Database
	private Database db;
	private Relation<LGraph<Attributes,Attributes>> grel;
	private Relation<SparseNumberVector> rel;
	private DBIDRange dbidmap;
	
	private PrioritySearcher<DBIDRef> osearcher;
	private PrioritySearcher<DBIDRef> searcher;
	
	
	/**
	 * @param ds dataset
	 * @param vectorDist vectorDistance (used to filter candidates), that should be a lower bound to ged (so it guarantees correctness)
	 * IMPORTANT: vectorDist.compute() is not used to compute the distance between objects
	 * @param specifies, whether the vectors should be stored as integer vectors or doubles
	 */
	public LowerBoundIndex(AttrDataset ds, GraphEditCostsVectorDistance vectorDist,	 boolean integerVec){	
		
		this.ds = ds;
		this.vecDist = vectorDist;
		this.integerVec = integerVec;
		
			HashMap<LGraph<Attributes, Attributes>, SparseFeatureVector<Integer>> vectorMap = vecDist.getVectorMap();
			int maxEntry = vecDist.getMaxEntry();
			
			//use specified database connection (integer or double vectors)
			DatabaseConnection dbconnection;
			if(this.integerVec)
			{
				dbconnection = new IntegerVectorDataConnection(ds,vectorMap,maxEntry);
			}
			else
			{
				dbconnection = new SparseVectorDataConnection(ds,vectorMap,maxEntry);
			}
			// INITIALIZE DB
			
			this.db = new ELKIBuilder<>( StaticArrayDatabase.class) //
					.with(StaticArrayDatabase.Par.DATABASE_CONNECTION_ID,
							dbconnection)
					.with(StaticArrayDatabase.Par.INDEX_ID, CoverTree.Factory.class)
	                .with(CoverTree.Factory.Par.DISTANCE_FUNCTION_ID, SparseManhattanDistance.STATIC)
	                .with(CoverTree.Factory.Par.EXPANSION_ID, 1.3)
	                .with(CoverTree.Factory.Par.TRUNCATE_ID, 5)
					.build();
			db.initialize();
			// Relation containing the graphs:
			this.grel = db.getRelation(GRAPH_TYPE);
			// Relation containing the number vectors:
			this.rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);
			
			// Relation containing graphids
			this.dbidmap = (DBIDRange) this.rel.getDBIDs();
			
			if(this.integerVec)
			{
				this.distanceFactor = ((IntegerVectorDataConnection) dbconnection).getFactor();
			}
			else // for the double vectors, the entries are not changed, so there is no need to set it to any other number than 1
			{
				this.distanceFactor = 1;
			}
			
			osearcher = new QueryBuilder<>(rel,SparseManhattanDistance.STATIC).priorityByDBID();
			searcher = new ExactPrioritySearcher<DBIDRef>(osearcher);
			

	}
	
	/**
	 * Function for getting the candidates to a range query from elki
	 * @param queryObjectID id of the query object
	 * @param threshold range threshold of the similarity search query
	 * @return a list of all candidates, that have an approximate distance to the query object, that is smaller than the given threshold
	 */
	public IntArrayList getCandidates(Integer queryObject, double threshold) {
		IntArrayList candidates = new IntArrayList();
		DBIDRef qit = dbidmap.iter().seek(queryObject);
		double thresholdWithFactor = threshold*this.distanceFactor;
		
		//find similar graphs in database
		searcher.decreaseCutoff(thresholdWithFactor);
		for(searcher.search(qit, thresholdWithFactor); searcher.valid(); searcher.advance())
		{
			if(searcher.getUpperBound() <= thresholdWithFactor|| searcher.computeExactDistance() <= thresholdWithFactor)
			{	
				candidates.add(dbidmap.getOffset(searcher));
			}
		}		
		return candidates;
	}

	
	/**
	 * Function that searches the k nearest neighbors to a given query graph
	 * @param queryObjectID the ID of the query graph
	 * @param k the number of nearest neighbors that are to be found
	 * @param distance the (exact) verifier that is to be used
	 * @param lowerbounds additional lowerbounds used to filter
	 * @return the k nearest neighbors (or more than k, if they are at the same distance)
	 */
	public IntArrayList kNNSearch(Integer query, int k, GraphEditDistanceVerifier<Attributes,Attributes> distance, ArrayList<Distance<LGraph<Attributes,Attributes>>> lowerbounds)
	{
		return kNNSearchDetail(query, k, distance, lowerbounds).getFirst();
	}
	
	/**
	 * @param cand list of IDs 
	 * @return list of graphs corresponding to IDs
	 */
	public ArrayList<LGraph<Attributes,Attributes>> getSubset(IntArrayList cand) {
		
		ArrayList<LGraph<Attributes,Attributes>> subset = new ArrayList<LGraph<Attributes,Attributes>>();
		DBIDArrayIter iter = dbidmap.iter();
		for (IntIterator it = cand.iterator(); it.hasNext(); )
		{
		  subset.add(grel.get(iter.seek(it.nextInt())));
		}
		return subset;
	}
	
	public GraphEditCostsVectorDistance getVecDist() {
		return this.vecDist;
	}

	/**
	 * @param graphID ID
	 * @return graph with ID graphID
	 */
	public LGraph<Attributes,Attributes> getGraph(Integer graphID) 
	{
		DBIDRef qit = dbidmap.iter().seek(graphID);
		return this.grel.get(qit);
	}
	
	
	/**
	 * Function that searches the k nearest neighbors to a given query graph
	 * @param queryObjectID the ID of the query graph
	 * @param k the number of nearest neighbors that are to be found
	 * @param distance the (exact) verifier that is to be used
	 * @param lowerbounds additional lowerbounds used to filter
	 * @return Triple consisting of kNNs, #candidates produced by vecDist, #candidates that had to be refined 
	 * 
	 */
	public Triple<IntArrayList,Integer,Integer> kNNSearchDetail(Integer query, int k, GraphEditDistanceVerifier<Attributes,Attributes> distance, ArrayList<Distance<LGraph<Attributes,Attributes>>> lowerbounds)
	{
		
		KNNHeap heap = DBIDUtil.newHeap(k);
		double threshold = Double.MAX_VALUE;
		DBIDRef qit = dbidmap.iter().seek(query); 
		LGraph<Attributes,Attributes> qgraph = this.grel.get(qit);
		double thresholdWithFactor = threshold*this.distanceFactor;
		
		// just for logging
		int refinedcandidates = 0; // refined candidates
		int candidates = 0; // all candidates
		
		//find similar graphs in database
		searcher.decreaseCutoff(thresholdWithFactor);
		for(searcher.search(qit, thresholdWithFactor); searcher.valid(); searcher.advance())
		{
			if(searcher.getUpperBound() <= thresholdWithFactor || searcher.computeExactDistance() <= thresholdWithFactor)
			{
				candidates++;
				boolean can = true;
				if(heap.size()>=k)
				{
					//try to filter it out using other lower bounds first
					for(Distance<LGraph<Attributes,Attributes>> lowerbound: lowerbounds)
					{
						if(can)
						{
							if(lowerbound.compute(qgraph, this.grel.get(searcher)) > threshold) //TODO: if using other lowerbounds, would the possibility of a distance factor be beneficial?
							{
								can = false; //graph can be filtered out
							}
						}
					}
					
				}
				if(can)
				{
					refinedcandidates++;
					double d = distance.compute(qgraph, this.grel.get(searcher),threshold); //refine candidate
					if(d!= -1 && d<=threshold) //d should not be greater than threshold, because then it should be -1 (verifier)
					{
						heap.insert(d,searcher);
					
						if(heap.size()>=k) //adjust threshold, if at least k neighbors are found
						{	
							threshold = heap.peekKey();
							thresholdWithFactor = threshold *this.distanceFactor;
							searcher.decreaseCutoff(thresholdWithFactor);
						}
					}
				}
			}
		}
		
		//System.out.println(candidates + " candidates had to be refined.");
		
		// add the k nearest neighbors to the result set
		IntArrayList nearestNeighbors = new IntArrayList();
		for(DoubleDBIDIter it = heap.unorderedIterator(); it.valid(); it.advance())
		{
			nearestNeighbors.add(dbidmap.getOffset(it));
		}
		return new Triple<IntArrayList,Integer,Integer>(nearestNeighbors,candidates,refinedcandidates);
	}

	/**
	 * 
	 * THIS METHOD DOES NOT USE EMBASSI
	 * This method is only there to compare other filters to EmbAssi regarding their performance in multi-step knn.
	 * It is here because having the KNNHeap is very nice!
	 * @param queryObjectID the ID of the query graph
	 * @param k the number of nearest neighbors that are to be found
	 * @param distance the (exact) verifier that is to be used
	 * @param otherDist distance that is used as the ONLY filter
	 * @return Triple consisting of kNNs, #candidates produced by OtherDist (that had to be refined) (and again) #candidates that had to be refined 
	 */
	public Triple<IntArrayList, Integer, Integer> kNNSearchComparison(Integer queryObjectID, int k, GraphEditDistanceVerifier<Attributes,Attributes> distance, Distance<LGraph<Attributes, Attributes>> otherDist) {

		KNNHeap heap = DBIDUtil.newHeap(k);
		double threshold = Double.MAX_VALUE;
		DBIDRef qit = dbidmap.iter().seek(queryObjectID); 
		LGraph<Attributes,Attributes> qgraph = this.grel.get(qit);
		
		// just for logging
		int refinedcandidates = 0; // refined candidates
		int candidates = 0; // all candidates
		
		
		//first OtherDist(queryGraph, g) has to be computed for all g in DB
		ArrayList<Triple<Integer,Double,Integer>> list = new ArrayList<Triple<Integer,Double,Integer>>();
		for(DBIDArrayIter it = dbidmap.iter(); it.valid(); it.advance())
		{
			double dist = otherDist.compute(qgraph, this.grel.get(it));
			list.add(new Triple<Integer,Double,Integer> (it.getOffset(),dist,0));
			
		}
		
		//sort db objects by  otherDist to queryGraph
		Collections.sort(list,new Comparator<Triple<Integer,Double,Integer>>() {
		    @Override
		    public int compare(Triple<Integer,Double,Integer> o1, Triple<Integer,Double,Integer> o2) {
		        return o1.getSecond().compareTo(o2.getSecond());
		    }
		});
		
		for(Triple<Integer, Double, Integer> t:list)
		{
			if(t.getSecond()<=threshold)
			{
				candidates++;
				refinedcandidates++;
				DBIDRef nit = dbidmap.iter().seek(t.getFirst()); 
				double d = distance.compute(qgraph,this.grel.get(nit),threshold); //refine candidate
				if(d!=-1 &&d<=threshold) 
				{
					heap.insert(d,nit);
					if(heap.size()>=k) //adjust threshold, if at least k neighbors are found
					{	
						threshold = heap.peekKey();
					}
				}
			}
			else
			{
				break;
			}
		}
		
		//System.out.println(candidates + " candidates had to be refined.");
		
		// add the k nearest neighbors to the result set
		IntArrayList nearestNeighbors = new IntArrayList();
		for(DoubleDBIDIter it = heap.unorderedIterator(); it.valid(); it.advance())
		{
			nearestNeighbors.add(dbidmap.getOffset(it));
		}
		return new Triple<IntArrayList,Integer,Integer>(nearestNeighbors,candidates,refinedcandidates);
	}

}
