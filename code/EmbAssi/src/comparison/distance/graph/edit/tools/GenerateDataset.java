package comparison.distance.graph.edit.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import graph.ConnectivityTools;
import graph.LGraph;
import graph.LGraphTools;
import graph.Graph.Edge;
import graph.Graph.Vertex;
import graph.attributes.AttributedGraph;
import graph.attributes.Attributes;
import graph.properties.EdgeArray;
import graph.properties.VertexArray;
import io.AttributedGraphSetWriter;
import io.AttributedGraphToBSSGEDFormatWriter;
import io.AttributedGraphToMLIndexFormatWriter;

/**
 * Class for generating datasets for experiments (generating random queries and writing graphs to the file format of the other (c++) methods)
 * @author bause
 *
 */
public class GenerateDataset {
	public static void generateDataset(ArrayList<AttributedGraph> ds, String path, String dsName, int queries) throws IOException {
		
		generateDataset(ds,path,dsName,generateNumbers(queries,ds.size()));
	}
	public static void writeQueries(ArrayList<Integer> numbers, String filename)  throws IOException{
		File newFile = new File(filename+".txt");
		BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
		for(Integer i: numbers)
		{
			writer.write(i.toString());
			writer.newLine();
		}
		
		writer.close();
	}
	public static ArrayList<Integer> generateNumbers(int n, int range)
	{
		ArrayList<Integer> numbers = new ArrayList<Integer>();
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
			System.out.println("Dataset too small "+ numbers.size() + " instead of " + n);
		}
		return numbers;
	}
	public static void generateDataset(ArrayList<AttributedGraph> ds, String path, String dsName, ArrayList<Integer> numbers) throws IOException {
		
		String foldername = "/"+dsName+"_"+numbers.size()+"/";
		//create folder
		File dir = new File(path+foldername);
		if(!dir.exists())
		{
			dir.mkdir();
		}
		//write graphs (evtl not necessary)
		AttributedGraphSetWriter writer = new AttributedGraphSetWriter(path+foldername,dsName);
		writer.write(ds, dsName,path+foldername);
		File qdir = new File(path+foldername+"queries/");
		if(!qdir.exists())
		{
			qdir.mkdir();
		}
		
		writeQueries(numbers,path+foldername+"queries/"+numbers.size()+"_queries");
	}
	
	public static void generatefullDataset(ArrayList<AttributedGraph> ds, String path, String dsName,boolean edgelabels) throws IOException {
		
		String foldername = "/"+dsName+"/";
		//create folder
		File dir = new File(path+foldername);
		if(!dir.exists())
		{
			dir.mkdir();
		}
		
		//remove edge labels
		if(!edgelabels)
		{
			for(AttributedGraph g: ds)
			{
				EdgeArray<Attributes> ea = new EdgeArray<Attributes>(g.getGraph());
				for(Edge e: g.getGraph().edges())
				{
					ea.set(e, new Attributes("0"));
				}
				g.setEdgeLabel(ea);
			}
		}
		//only keep one nominal vertex label
		if(ds.get(0).getVertexLabelCount()>1)
		{
			for (AttributedGraph g : ds) {
				VertexArray<Attributes> va = new VertexArray<Attributes>(g.getGraph());
				for (Vertex v : g.getGraph().vertices()) {
					va.set(v, new Attributes((String) g.getVertexLabel().get(v).getNominalAttribute(0)));
				}
				g.setVertexLabel(va);
			}
		}
		
		
		
		AttributedGraphSetWriter writer = new AttributedGraphSetWriter(path,dsName);
		writer.write(ds, dsName,path);
		//AttributedGraphToSDFWriter.write(ds,path+foldername,dsName);
		
		ArrayList<Integer> numbers = generateNumbers(100,ds.size());
		writeQueries(numbers,path+foldername+"queries");
		
		//write dataset (and queries) to other format
		//AttributedGraphToOtherFormatWriter.write(ds,path+foldername,dsName+".txt");
		AttributedGraphToMLIndexFormatWriter.write(ds,path+foldername,dsName+"_ML.txt");
		AttributedGraphToBSSGEDFormatWriter.write(ds,path+foldername,dsName+"_bssged.txt");
		ArrayList<AttributedGraph> temp = new ArrayList<AttributedGraph>();
		for(int i : numbers)
		{
			temp.add(ds.get(i));
		}

		//AttributedGraphToOtherFormatWriter.write(temp,path+foldername,dsName+"_queries.txt");
		AttributedGraphToMLIndexFormatWriter.write(temp,path+foldername,dsName+"_ML_queries.txt");
		AttributedGraphToBSSGEDFormatWriter.write(temp,path+foldername,dsName+"_bssged_queries.txt");
		}

	public static void generateSubsets(ArrayList<AttributedGraph> ds, String path, String dsName, boolean edgelabels) throws IOException {
		for(int i = 100; i< ds.size()&&i<400; i+=100)
		{
			ArrayList<AttributedGraph> temp = new ArrayList<AttributedGraph>();
			ArrayList<Integer> numbers = generateNumbers(i, ds.size());
			for(int n : numbers)
			{
				temp.add(ds.get(n));
			}
			generatefullDataset(temp, path, dsName+"_"+i, edgelabels);
		}
	}
	public static void generateConnectedDataset(ArrayList<AttributedGraph> ds, String path, String dsName, boolean edgelabels) throws IOException {
		ArrayList<AttributedGraph>  dsNew = new ArrayList<AttributedGraph>();
		for(AttributedGraph g : ds)
		{
			if(ConnectivityTools.isConnected(g.getGraph()))
			{
				dsNew.add(g);
			}
			else
			{
				
				ArrayList<LinkedList<Vertex>> comp = ConnectivityTools.connectedComponents(g.getGraph());
				LinkedList<Vertex> biggestComponent = comp.get(0);
				for(LinkedList<Vertex> c :comp)
				{
					if(c.size() > biggestComponent.size())
					{
						biggestComponent = c;
					}
				}
				LGraph<String, String> gNew = LGraphTools.createInducedLabeledSubgraph(g,biggestComponent);
				AttributedGraph gNewer = AttributedGraph.convert(gNew);
				dsNew.add(gNewer);
			}
		}
		generatefullDataset(dsNew,path,dsName,edgelabels);
	}
}
