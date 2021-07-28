package cli;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Random;

import benchmark.dataset.AttrDataset;
import comparison.distance.Distance;
import comparison.distance.graph.edit.bounds.LabelCount;
import comparison.distance.graph.edit.bounds.branch.Branch;
import comparison.distance.graph.edit.bounds.cstar.CStarLowerBound;
import comparison.distance.graph.edit.bounds.exact.BSSGraphEditDistance;
import comparison.distance.graph.edit.bounds.exact.GraphEditDistanceVerifier;
import comparison.distance.graph.edit.vectordistance.DistanceBuilder;
import comparison.distance.graph.edit.vectordistance.LowerBoundIndex;
import comparison.distance.graph.edit.vectordistance.distances.GraphEditCostsCombinedVectorDistance;
import comparison.distance.graph.edit.vectordistance.distances.SimpleGraphEditCosts;
import comparison.distance.graph.edit.vectordistance.experiments.reference.BranchSimilaritySearchEmbAssi;
import comparison.distance.graph.edit.vectordistance.experiments.reference.CStarSimilaritySearchVariantEmbAssi;

import comparison.distance.graph.edit.vectordistance.experiments.reference.LinDSimilaritySearchEmbAssi;
import comparison.distance.graph.edit.vectordistance.experiments.reference.SLFSimilaritySearchEmbAssi;
import comparison.distance.graph.edit.vectordistance.experiments.reference.SimilaritySearchEmbAssiReference;
import comparison.distance.graph.edit.vectordistance.treedistances.DegreeLowerBound;
import comparison.distance.graph.edit.vectordistance.treedistances.LabelLowerBound;
import comparison.distance.tree.TreeDistance;
import datastructure.Triple;
import graph.LGraph;
import graph.Graph.Vertex;
import graph.attributes.Attributes;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class ExperimentsEmbAssi {
	
	private AttrDataset ds;
	private boolean knn;
	private boolean clb;
	private String method;
	private SimilaritySearchEmbAssiReference<Attributes, Attributes> ref;
	private LowerBoundIndex lbi;
	private SimpleGraphEditCosts<Attributes, Attributes> gec;
	private String fileName;

	public ExperimentsEmbAssi(String method, boolean clb, AttrDataset ds, boolean knn, String fileName) throws IOException {
		this.clb = clb;
		this.knn = knn;
		this.method = method;
		this.ds = ds;
		this.gec = new SimpleGraphEditCosts<Attributes, Attributes>();
		this.fileName = fileName;
		
		
		switch(this.method) 
		{
			case "CLB": //for clb, we only need to use the index
				this.clb = true;
				break;
			case "CStar":
				this.ref = new CStarSimilaritySearchVariantEmbAssi<Attributes, Attributes>(gec);
				break;
			case "Branch":
				this.ref = new BranchSimilaritySearchEmbAssi<Attributes, Attributes>(gec);
				break;
			case "SLF":
				this.ref = new SLFSimilaritySearchEmbAssi<Attributes, Attributes>(gec);
				break;
			case "LinD":
				BufferedWriter tw = new BufferedWriter( new FileWriter(this.fileName, true));
				long timeStart = System.nanoTime();
				this.ref = new LinDSimilaritySearchEmbAssi<Attributes, Attributes>(gec,this.ds);
				long timeEnd = System.nanoTime();
				long time = timeEnd - timeStart;
				tw.write("Preprocessing LinD "+String.format(Locale.ENGLISH, "%1.2f", (double)time/(double)1000000000));
				tw.newLine();
				tw.close();
				break;
			default:
				System.err.println("Unknown method, using (only) CLB");
				this.clb = true;
				break;
		}
		if(this.clb)
		{
			BufferedWriter tw = new BufferedWriter( new FileWriter(fileName, true));
			
			
			long timeStart = System.nanoTime();
			ArrayList<TreeDistance<Vertex>> dists = new ArrayList<TreeDistance<Vertex>>();
			dists.add(new LabelLowerBound(ds, gec));
			dists.add(new DegreeLowerBound(ds, gec));
			GraphEditCostsCombinedVectorDistance vectorDistance = new GraphEditCostsCombinedVectorDistance(ds, dists);
			this.lbi = new LowerBoundIndex(ds, vectorDistance, false);
			long timeEnd = System.nanoTime();
			long time = timeEnd - timeStart;
			tw.write("Preprocessing EmbAssi "+String.format(Locale.ENGLISH, "%1.2f", (double)time/(double)1000000000));
			tw.newLine();
			tw.close();
		}
	}

	public void run(IntArrayList queries, int range) throws IOException
	{
		if(knn)
		{
			knnsearch(queries, range);
		}
		else
		{
			rangesearch(queries, range);
		}
	}

	private void rangesearch(IntArrayList queries, int range) throws IOException {
		BufferedWriter tw = new BufferedWriter( new FileWriter(fileName, true));
		tw.write("threshold		candidates		time in ms for "+ queries.size()+ " queries \n");
		ArrayList<ArrayList<LGraph<Attributes,Attributes>>> candidates= new ArrayList<ArrayList<LGraph<Attributes,Attributes>>>();
		double[] filtertime = new double[range];
		if(this.clb)
		{
			tw.write("clb \n");
			for(int threshold = 1; threshold <=range; threshold++)
			{
				tw.write(threshold + "		");
				int indexCandidates =0;
				long indexTime = 0;
				for(Integer q1:queries)
				{
					long timeStartI = System.nanoTime();
					IntArrayList cand = lbi.getCandidates(q1, threshold);
					long timeEndI = System.nanoTime();
					long timeI = timeEndI - timeStartI;
					indexTime += timeI;
					indexCandidates += cand.size();
					ArrayList<LGraph<Attributes, Attributes>> filtered = lbi.getSubset(cand);
					candidates.add((ArrayList<LGraph<Attributes, Attributes>>) filtered.clone());
				}
			filtertime[threshold-1] = (double)indexTime;
			tw.write(String.format(Locale.ENGLISH, "%1.2f", (double)indexCandidates/(double)queries.size())+ "		");
			tw.write(String.format(Locale.ENGLISH, "%1.2f", (double)indexTime/(double)1000000)+ "\n");
			}
			tw.flush();
			System.out.println("CLB is done!");
		}
		tw.newLine();
		if(this.ref!=null)
		{
			ArrayList<LGraph<Attributes, Attributes>> db = new ArrayList<LGraph<Attributes,Attributes>>(ds);
			int index=0;
			for(int threshold = 1; threshold <=5; threshold++)
			{
				tw.write(threshold + "		");
				int allCandidates =0;
				long fulltime = 0;
				for(Integer q1:queries)
				{
					LGraph<Attributes, Attributes> qg = ds.get(q1);
					if(clb) { //use candidates that were already filtered
						db =candidates.get(index);
					}
					long timeStart = System.nanoTime();
					Collection<LGraph<Attributes, Attributes>> can = this.ref.search(qg, threshold, db);
					long timeEnd = System.nanoTime();
					long time = timeEnd - timeStart;
					fulltime += time;
					allCandidates += can.size();
					index++;
				}
				tw.write(String.format(Locale.ENGLISH, "%1.2f", (double)allCandidates/(double)queries.size())+ "  ");
				tw.write(String.format(Locale.ENGLISH, "%1.2f", ((double)fulltime+filtertime[threshold-1])/(double)1000000)+ " \n");
			}
		}
		tw.flush();
		System.out.println("All done!");
		tw.newLine();
		tw.close();
	}
	private void knnsearch(IntArrayList queries, int k) throws IOException {
		BufferedWriter tw = new BufferedWriter( new FileWriter(fileName, true));
		tw.write("k		candidates		results		time in ms for "+ queries.size()+ " queries \n");
		GraphEditDistanceVerifier<Attributes,Attributes> tempDist = new BSSGraphEditDistance<Attributes, Attributes>(10);
		if(this.clb)
		{
			tw.write("clb \n");
			for(int threshold = 1; threshold <=k; threshold++)
			{
				tw.write(threshold + "		");
				long fulltime = 0;
				int candidates = 0;
				int results = 0;
				for(Integer query:queries)
				{
					System.out.print("*");
					long timeStart = System.nanoTime();
					Triple<IntArrayList, Integer, Integer> result = lbi.kNNSearchDetail(query, threshold, tempDist, new ArrayList<Distance<LGraph<Attributes,Attributes>>>());
					long timeEnd = System.nanoTime();
					long time = timeEnd - timeStart;
					fulltime += time;
					candidates += result.getSecond();
					results += result.getFirst().size();
				}
				System.out.println();
				tw.write(String.format(Locale.ENGLISH, "%1.2f",((double)candidates)/((double)queries.size())-1)+ "		");
				tw.write(String.format(Locale.ENGLISH, "%1.2f",(((double)results)/((double)queries.size()))-1)+ "		");
				tw.write(String.format(Locale.ENGLISH, "%1.2f", (double)fulltime/(double)1000000000)+ "\n");
			}
			tw.flush();
			System.out.println("CLB is done!");
		}
		tw.newLine();
		if(this.ref!=null)
		{
			if(!this.clb) //this is just for easier knn search using the KNNHeap
			{
				ArrayList<TreeDistance<Vertex>> dists = new ArrayList<TreeDistance<Vertex>>();
				dists.add(new LabelLowerBound(ds, gec));
				dists.add(new DegreeLowerBound(ds, gec));
				GraphEditCostsCombinedVectorDistance vectorDistance = new GraphEditCostsCombinedVectorDistance(ds, dists);
				this.lbi = new LowerBoundIndex(ds, vectorDistance, false);
			}
			tw.write(this.method +" \n");
			Distance<LGraph<Attributes,Attributes>> otherDist;
			switch(this.method) 
			{
				case "CStar":
					otherDist = new CStarLowerBound();
					break;
				case "Branch":
					otherDist = new Branch(gec);
					break;
				case "SLF":
					otherDist = new LabelCount(gec);
					break;
				case "LinD":
					System.err.println("LinD is not a lower bound");
					return;
				default:
					System.err.println("Unknown method");
					return;
			}
			for(int threshold = 1; threshold <=k; threshold++)
			{
				tw.write(threshold + "		");
				long fulltime = 0;
				int candidates = 0;
				int results = 0;
				for(Integer query:queries)
				{
					System.out.print("*");
					long timeStart = System.nanoTime();
					Triple<IntArrayList, Integer, Integer> result = lbi.kNNSearchComparison(query, threshold, tempDist, otherDist);
					long timeEnd = System.nanoTime();
					long time = timeEnd - timeStart;
					fulltime += time;
					candidates += result.getSecond();
					results += result.getFirst().size();
				}
				System.out.println();
				tw.write(String.format(Locale.ENGLISH, "%1.2f",((double)candidates)/((double)queries.size())-1)+ "		");
				tw.write(String.format(Locale.ENGLISH, "%1.2f",(((double)results)/((double)queries.size()))-1)+ "		");
				tw.write(String.format(Locale.ENGLISH, "%1.2f", (double)fulltime/(double)1000000000)+ "\n");
			}
		}
		tw.flush();
		System.out.println("All done!");
		tw.newLine();
		tw.close();
	}

	
	public static IntArrayList generateNumbers(int n, int range)
	{
		IntArrayList numbers = new IntArrayList();
		Random rand = new Random();
		while(numbers.size()<n && numbers.size()<range)
		{
			int next = rand.nextInt(range);
			if(!numbers.contains(next)) //only use each graph once
			{
				numbers.add(next);
			}
			
		}
		if(numbers.size()<n)
		{
			System.err.println("Dataset too small "+ numbers.size() + " instead of " + n);
		}
		return numbers;
	}
	
	public static void plotapprox(AttrDataset ds , int numQueries, String filename) throws IOException{
		// load dataset
		SimpleGraphEditCosts<Attributes, Attributes> gec = new SimpleGraphEditCosts<Attributes, Attributes>();
		IntArrayList queries = new IntArrayList(generateNumbers(numQueries, ds.size()));
		BufferedWriter tw = new BufferedWriter( new FileWriter(filename, true));
		
		String exactDist = "exactLT";

		String[] dists = { "clb", "llb", "dlb", "elb", "slf", "cstarlb", "branch","branchub","blplb","linD","beamD", "cstarub","cstarubref"};

		tw.write("% time in nanoseconds, relative approx error)\n");
		tw.write(exactDist +" &");
		Distance<LGraph<Attributes, Attributes>> tempDist = DistanceBuilder.getDistance(ds, exactDist, gec);
		ArrayList<Double> distances = new ArrayList<Double>();
		int j = 0;
		long fulltime = 0;
		for (Integer q1 : queries) {
			for (Integer q2 : queries) {
				System.out.print(j);
				long timeStart = System.nanoTime();
				double distance = tempDist.compute(ds.get(q1), ds.get(q2));
				long timeEnd = System.nanoTime();
				long time = timeEnd - timeStart;
				fulltime += time;
				distances.add(distance);
				j++;
				if (j >= 100) {
					break;
				}
			}
			if (j >= 100) {
				break;
			}
		}
		tw.write("" + String.format(Locale.ENGLISH, "%1.2f", (double) fulltime / (double) j) + "&"
				+ String.format(Locale.ENGLISH, "%1.2f", 0.00) + "\\\\");
		tw.newLine();

		System.out.println();

		for (String dist : dists) {
			int i = 0;
			System.out.println(dist);
			long approxtime = 0;
			ArrayList<Double> approx = new ArrayList<Double>();

			tw.write("" + dist + " & ");
			tempDist = DistanceBuilder.getDistance(ds, dist, gec);

			for (Integer q1 : queries) {
				for (Integer q2 : queries) {

					long timeStart = System.nanoTime();
					double distance = tempDist.compute(ds.get(q1), ds.get(q2));
					long timeEnd = System.nanoTime();
					long time = timeEnd - timeStart;
					approxtime += time;
					approx.add(distance);

					i++;
					if (i >= 100) {
						break;
					}
				}
				if (i >= 100) {
					break;
				}
			}
			double score = 0;
			int notZero = 0;
			for (int v = 0; v < distances.size(); v++) {
				if(distances.get(v)==0 && approx.get(v)!= 0)
				{
					System.out.println("Bad approximation!");
					notZero++;
				}
				if(distances.get(v)>0)
				{
					score += Math.abs(approx.get(v) - distances.get(v))/distances.get(v);
				}
				
			}
			tw.write("" + String.format(Locale.ENGLISH, "%1.2f", (double) approxtime / (double) i) + " & "
					+ String.format(Locale.ENGLISH, "%1.2f", score / (double) i) + "\\\\");
			
			tw.write(" % "+ notZero);
			tw.newLine();
			tw.flush();

		}
		tw.newLine();
		tw.close();
	}
	
}
