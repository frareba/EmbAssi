package io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

import benchmark.dataset.LGDataset;
import graph.Graph;
import graph.Digraph.DiEdge;
import graph.Graph.Edge;
import graph.Graph.Vertex;
import graph.attributes.AttributedGraph;
import graph.attributes.Attributes;
import graph.properties.EdgeArray;
import graph.properties.VertexArray;


/**
 * @author kriege, bause
 * A class to write an AttributedGraph set
 * It is possible to write one graph at a time (for large data sets) 
 * and to create multiple data sets using the same label maps
 * 
 */
public class AttributedGraphSetWriter {

	public static void main(String[] args) throws IOException {
		String path = "/home/bause/Schreibtisch/WriterTest/Cuneiform/";
		String datasetName = "Cuneiform";
		AttributedGraphSetReader reader = new AttributedGraphSetReader();
		LGDataset<AttributedGraph> dsRef = reader.read(path+datasetName);
		
		AttributedGraphSetWriter agwTest = new AttributedGraphSetWriter("/home/bause/Schreibtisch/WriterTest/Test/",datasetName);
		for(AttributedGraph g: dsRef)
		{
			agwTest.writeGraph(g);
		}
		
		AttributedGraphSetWriter test = new AttributedGraphSetWriter();
		test.write(dsRef, "Test", "/home/bause/Schreibtisch/WriterTest/Test/");

	}

	// adding to file or create new ones
	private boolean adding;
	// have label readme and statistics been written yet?
	private boolean finished;
	private boolean newLabels;
	
	private	boolean hasNodeLabels;
	private	boolean hasNodeAttributes;
	private	boolean hasEdgeLabels;
	private	boolean hasEdgeAttributes;
	private	boolean hasGraphSets;
	
	// have the same label maps for multiple data sets
	private	LabelVectorConverter vlvc;
	private	LabelVectorConverter elvc;
	private	LabelToIntStringConverter classlc;
	private	LabelToIntStringConverter setlc;
	
	// for statistics, if the graph labels are integers already
	private ArrayList<Integer> graphLabels;
	
	
	private String datasetName;
	private String targetPath;
	
	// attributes for correct adding of graphs and statistics
	private int globalVertexIndex;
	private long globalVertexCount;
	private long globalGraphIndex;
	private long numberOfGraphs;
	private long edgecount;
	
	/**
	 * @param targetPath the path where the data set will be saved
	 * @param datasetName the name of the data set
	 */
	public AttributedGraphSetWriter(String targetPath, String datasetName)
	{
		this.adding = false;
		this.newLabels= true;
		this.finished = false;
	
		this.datasetName = datasetName;
		this.targetPath = targetPath;
		
		this.globalVertexCount = 0;
		this.globalGraphIndex = 1;
		this.globalVertexIndex = 1;
		this.numberOfGraphs = 0;
		this.edgecount = 0;
		
	}
	
	/**
	 * empty constructor to allow usage just like the old class
	 * only for usage with functions "write(...)"
	 * datasetName = "Empty"
	 * targetPath = ""
	 */
	public AttributedGraphSetWriter()
	{
		this("","Empty");
	}
	
	/**
	 * function to write graphs one at a time to a data set (writing the first graph will create the data set)
	 * after writing the last graph, call finishSet() to write the label readme and statistics
	 * 
	 * @param ag graph to be written to the current data set
	 * @throws IOException
	 */
	public void writeGraph(AttributedGraph ag) throws IOException
	{
		// using new label? then the label maps have to be initialized
		if(this.newLabels)
		{
			this.newLabels = false;
			
			// configuration
			Attributes vertexAttr = null;
			Attributes edgeAttr = null;

			for (Vertex v : ag.getGraph().vertices()) {
				vertexAttr = ag.getVertexLabel().get(v);
				break;
			}
			for (Edge e : ag.getGraph().edges()) {
				edgeAttr = ag.getEdgeLabel().get(e);
				break;
			}
			
			this.hasNodeLabels = vertexAttr.hasNominalAttributes();
			this.hasNodeAttributes = vertexAttr.hasRealValuedAttributes();
			this.hasEdgeLabels = edgeAttr.hasNominalAttributes();
			this.hasEdgeAttributes = edgeAttr.hasRealValuedAttributes();
			this.hasGraphSets = ag.getGraph().getProperty("set") != null;
			
			// a label converter for each dimension of labels and classes
			this.vlvc = new LabelVectorConverter(vertexAttr.getNominalAttributeCount());
			this.elvc = new LabelVectorConverter(edgeAttr.getNominalAttributeCount());
			this.classlc = new LabelToIntStringConverter();
			this.setlc = new LabelToIntStringConverter(AttributedGraphSetReader.GRAPH_SET_MAP);
			
			this.graphLabels = new ArrayList<Integer>();
			
		}
		String prefix = "";
		//init data set files
		if(this.targetPath=="")
		{
			prefix = this.datasetName+"/" + this.datasetName;
			File dir = new File(this.datasetName);
			if(!dir.exists())
			{
				dir.mkdir();
			}
		}
		else
		{
			prefix = this.targetPath + "/" + this.datasetName+"/" + this.datasetName;
			File dir = new File(this.targetPath + "/" + this.datasetName);
			if(!dir.exists())
			{
				dir.mkdir();
			}
		}
		
		File fASparse = new File(prefix+"_A.txt");
		File fNodeLabels = new File(prefix+"_node_labels.txt");
		File fGraphIndicator = new File(prefix+"_graph_indicator.txt");
		File fGraphLabels = new File(prefix+"_graph_labels.txt");
		File fEdgeLabels = new File(prefix+"_edge_labels.txt");
		
		File fNodeAttributes = new File(prefix+"_node_attributes.txt");
		File fGraphSets = new File(prefix+"_graph_sets.txt");
		File fEdgeAttributes = new File(prefix+"_edge_attributes.txt");
		
		//writer
		BufferedWriter wASparse = new BufferedWriter(new FileWriter(fASparse, this.adding));
		BufferedWriter wNodeLabels = new BufferedWriter(new FileWriter(fNodeLabels, this.adding));
		BufferedWriter wGraphIndicator = new BufferedWriter(new FileWriter(fGraphIndicator, this.adding));
		BufferedWriter wGraphLabels = new BufferedWriter(new FileWriter(fGraphLabels, this.adding));
		BufferedWriter wEdgeLabels = new BufferedWriter(new FileWriter(fEdgeLabels, this.adding));
		
		BufferedWriter wNodeAttributes = new BufferedWriter(new FileWriter(fNodeAttributes, this.adding));
		BufferedWriter wGraphSets = new BufferedWriter(new FileWriter(fGraphSets, this.adding));
		BufferedWriter wEdgeAttributes = new BufferedWriter(new FileWriter(fEdgeAttributes, this.adding));
		
		if(!this.adding) //after the first written graph: add to the data set
		{
			this.adding = true;
		}
		
		Graph g = ag.getGraph();
		VertexArray<Attributes> va = ag.getVertexLabel();
		EdgeArray<Attributes> ea = ag.getEdgeLabel();
		HashMap<Vertex,Integer> indexMap = new HashMap<Vertex, Integer>();
		
		// vertices and labels
		for (Vertex v : g.vertices()) {
			this.globalVertexCount++;
			indexMap.put(v, globalVertexIndex++);
			wGraphIndicator.append(String.valueOf(globalGraphIndex));
			wGraphIndicator.newLine();
			Attributes vAttr = va.get(v);

			wNodeLabels.append(vlvc.convert(vAttr.getNominalAttributes()));
			wNodeLabels.newLine();
			wNodeAttributes.append(obj2String(vAttr.getRealValuedAttributes()));
			wNodeAttributes.newLine();
		}
		// edges and labels
		for (Edge e : g.edges()) {
			this.edgecount++;
			
			String firstIndex = String.valueOf(indexMap.get(e.getFirstVertex()));
			String secondIndex = String.valueOf(indexMap.get(e.getSecondVertex()));
			wASparse.append(firstIndex+", "+secondIndex);
			wASparse.newLine();
			Attributes eAttr = ea.get(e);
			wEdgeLabels.append(elvc.convert(eAttr.getNominalAttributes()));
			wEdgeLabels.newLine();
			wEdgeAttributes.append(obj2String(eAttr.getRealValuedAttributes()));
			wEdgeAttributes.newLine();
			
			if (!(e instanceof DiEdge)) { // duplicate entries for other direction
				wASparse.append(secondIndex+", "+firstIndex);
				wASparse.newLine();
				wEdgeLabels.append(elvc.convert(eAttr.getNominalAttributes()));
				wEdgeLabels.newLine();
				wEdgeAttributes.append(obj2String(eAttr.getRealValuedAttributes()));
				wEdgeAttributes.newLine();
			}
		}

		// convert class label: use integers if present, otherwise map
		Object rawClass = g.getProperty("class");
		boolean isInt = true;
		try {
			Integer.valueOf(String.valueOf(rawClass));
		} catch (NumberFormatException e) {
			isInt = false;
		}
		String classLabel;
		if (isInt) {
			if (!classlc.isEmpty())
				throw new IOException("Interpretation of class labels ambigous!");
			classLabel = rawClass.toString();
			Integer label = Integer.parseInt(rawClass.toString());
			if(!this.graphLabels.contains(label)) // only important for statistics
			{
				this.graphLabels.add(label);
			}
			
		} else {
			classLabel = classlc.convert(rawClass);
		}
		wGraphLabels.append(classLabel);
		wGraphLabels.newLine();

		// convert graph set label
		if (hasGraphSets) {
			String graphSet = setlc.convert(g.getProperty("set"));
			wGraphSets.append(graphSet);
			wGraphSets.newLine();
		}

		this.globalGraphIndex++;
		this.numberOfGraphs++;

		// close files
		wASparse.close();
		wNodeLabels.close();
		wGraphIndicator.close();
		wGraphLabels.close();
		wEdgeLabels.close();
		
		wNodeAttributes.close();
		wGraphSets.close();
		wEdgeAttributes.close();
		
	}
	/**
	 * starts a new data set (with new name/target path) using the same label maps as the previous one
	 * 
	 * @param newTargetPath the path where the new data set will be saved
	 * @param newDatasetName the name of the new data set
	 * @throws IOException
	 */
	public void startNewSetWithSameLabels(String newTargetPath, String newDatasetName) throws IOException
	{
		if(!this.finished)
		{
			this.finishSet();
		}
		
		this.initializeNewSet(newTargetPath, newDatasetName);
	}
	
	/**
	 * starts a new data set (with new name/target path) using new label maps
	 * 
	 * @param newTargetPath the path where the new data set will be saved
	 * @param newDatasetName the name of the new data set
	 * @throws IOException
	 */
	public void startNewSetWithNewLabels(String newTargetPath, String newDatasetName) throws IOException
	{
		//finish the previous data set if necessary
		if(!this.finished)
		{
			this.finishSet();
		}
			
		this.initializeNewSet(newTargetPath, newDatasetName);
		
		//reset the other attributes (can only be done with a sample graph)
		this.newLabels= true;
	}
	
	/**
	 * finishes the data set, so that no new graphs can be added
	 * deletes unnecessary files, writes a label readme file and a statistics file
	 * @throws IOException
	 */
	public void finishSet() throws IOException
	{
		if(this.finished)
		{
			return;
		}
		this.finished = true;
		
		//delete unnecessary files
		String prefix = this.targetPath + "/" + this.datasetName+"/" + this.datasetName;
		File fNodeLabels = new File(prefix+"_node_labels.txt");
		File fEdgeLabels = new File(prefix+"_edge_labels.txt");
		File fNodeAttributes = new File(prefix+"_node_attributes.txt");
		File fGraphSets = new File(prefix+"_graph_sets.txt");
		File fEdgeAttributes = new File(prefix+"_edge_attributes.txt");
		
		if (!hasNodeLabels) fNodeLabels.delete();
		if (!hasNodeAttributes) fNodeAttributes.delete();
		if (!hasEdgeLabels) fEdgeLabels.delete();
		if (!hasEdgeAttributes) fEdgeAttributes.delete();
		if (!hasGraphSets) fGraphSets.delete();
		
		//write the readme for label and statistics
		this.writeLabelReadMe();
		this.writeStatistics();
	}
	
	private void initializeNewSet(String targetPath2, String datasetName2)
	{
		//reset everything but the label maps
		this.adding = false;
		this.finished = false;
		
		this.datasetName = datasetName2;
		this.targetPath = targetPath2;
		
		this.globalVertexCount = 0;
		this.globalGraphIndex = 1;
		this.globalVertexIndex = 1;
		this.numberOfGraphs = 0;
		this.edgecount = 0;
	}

	private void writeLabelReadMe() throws IOException {
		// write label conversion statistics
		String prefix="";
		if(this.targetPath=="")
		{
			prefix = this.datasetName+"/" + this.datasetName;
		}
		else
		{
			prefix = this.targetPath + "/" + this.datasetName+"/" + this.datasetName;
		}
		
		File fLabelReadme = new File(prefix+"_label_readme.txt");
		BufferedWriter wLabelReadme = new BufferedWriter(new FileWriter(fLabelReadme));
		
		wLabelReadme.write("README for data set "+ this.datasetName);
		wLabelReadme.newLine();
		
		//TODO: what does this do?
//				// attribute and label names
//				@SuppressWarnings("unchecked")
//				ArrayList<String> vertexLabelNames = (ArrayList<String>) ags.iterator().next().getGraph().getProperty("vertexLabelNames");
//				if (vertexLabelNames != null && !vertexLabelNames.isEmpty()) {
//					wLabelReadme.write("Node labels:\t\t"+vertexLabelNames.toString()+"\n\n");
//				}
//				@SuppressWarnings("unchecked")
//				ArrayList<String> vertexAttrNames = (ArrayList<String>) ags.iterator().next().getGraph().getProperty("vertexAttrNames");
//				if (vertexAttrNames != null && !vertexAttrNames.isEmpty()) {
//					wLabelReadme.write("Node attributes:\t"+vertexAttrNames.toString()+"\n\n");
//				}
//				@SuppressWarnings("unchecked")
//				ArrayList<String> edgeLabelNames = (ArrayList<String>) ags.iterator().next().getGraph().getProperty("edgeLabelNames");
//				if (edgeLabelNames != null && !edgeLabelNames.isEmpty()) {
//					wLabelReadme.write("Edge labels:\t\t"+edgeLabelNames.toString()+"\n\n");
//				}
//				@SuppressWarnings("unchecked")
//				ArrayList<String> edgeAttrNames = (ArrayList<String>) ags.iterator().next().getGraph().getProperty("edgeAttrNames");
//				if (edgeAttrNames != null && !edgeAttrNames.isEmpty()) {
//					wLabelReadme.write("Edge attributes:\t"+edgeAttrNames.toString()+"\n\n");
//				}

		// mappings
		if (!this.vlvc.isEmpty()) {
			wLabelReadme.write("Node labels were converted to integer values using this map:\n\n");
			wLabelReadme.write(this.vlvc.toString());
			wLabelReadme.write("\n\n");
		}
		if (!this.elvc.isEmpty()) {
			wLabelReadme.write("Edge labels were converted to integer values using this map:\n\n");
			wLabelReadme.write(this.elvc.toString());
			wLabelReadme.write("\n\n");
		}
		if (!this.classlc.isEmpty()) {
			wLabelReadme.write("Class labels were converted to integer values using this map:\n\n");
			wLabelReadme.write(this.classlc.toString());
			wLabelReadme.write("\n\n");
		}
		if (this.hasGraphSets) {
			wLabelReadme.write("Set memberships were converted to integer values using this map:\n\n");
			wLabelReadme.write(this.setlc.toString());
			wLabelReadme.write("\n\n");
		}

		wLabelReadme.close();
	}

	private void writeStatistics() throws IOException {
		String prefix="";
		if(this.targetPath=="")
		{
			prefix = this.datasetName+"/" + this.datasetName;
		}
		else
		{
			prefix = this.targetPath + "/" + this.datasetName+"/" + this.datasetName;
		}
		
		File stats = new File(prefix+"_statistics.txt");
		BufferedWriter wStats = new BufferedWriter(new FileWriter(stats));
		wStats.append("#Graphs: "+this.numberOfGraphs);
		wStats.newLine();
		
		if(this.classlc.map.keySet().size()==0)
		{
			wStats.append("#Classes: "+this.graphLabels.size());
		}
		else
		{
			wStats.append("#Classes: "+this.classlc.map.keySet().size());
		}
		
		wStats.newLine();
		wStats.append("#Vertices: "+this.globalVertexCount);
		wStats.newLine();
		wStats.append("#Edges: "+this.edgecount);
		wStats.newLine();
		wStats.append(" avg #Vertices: "+((double) this.globalVertexCount)/(double) this.numberOfGraphs);
		wStats.newLine();
		wStats.append(" avg #Edges: "+((double) this.edgecount)/(double) this.numberOfGraphs);
		wStats.newLine();
		
		wStats.close();
	}
		
	
	
	public static class LabelVectorConverter extends ArrayList<LabelConverter> {
		
		public LabelVectorConverter(int n) {
			super(n);
			for (int i=0; i<n; i++) {
				add(new LabelToIntStringConverter());
			}
		}
		
		public String convert(Object[] os) {
			StringBuffer sb = new StringBuffer();
			if (os.length > 0) {
				sb.append(get(0).convert(os[0]));
			}
			for (int i=1; i<os.length; i++) {
				sb.append(", ");
				sb.append(get(i).convert(os[i]));
			}
			return sb.toString();
		}
		
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<size(); i++) {
				LabelConverter lc = get(i);
				sb.append("Component "+i+":\n");
				sb.append(lc.toString());
				sb.append("\n");
			}
			return sb.toString();
		}
		
		public boolean isEmpty() {
			for (int i=0; i<size(); i++) {
				if (!get(i).isEmpty()) return false;
			}
			return true;
		}
	}
	
	
	public static interface LabelConverter {
		
		public abstract String convert(Object o);		
		public abstract String toString();
		public abstract boolean isEmpty();
	}
	
	public static class LabelToStringValueConverter implements LabelConverter {
		public String convert(Object o) {
			return String.valueOf(o);
		}
		
		@Override
		public String toString() {
			return "Labels were converted using String.valueOf()";
		}

		@Override
		public boolean isEmpty() {
			return false;
		}
	}

	
	public static class LabelToIntStringConverter implements LabelConverter {
		
		HashMap<Object, Integer> map;
		
		public LabelToIntStringConverter() {
			this(new HashMap<Object, Integer>());
		}
		
		public LabelToIntStringConverter(HashMap<Object, Integer> map) {
			this.map = map;
		}
		
		public String convert(Object o) {
			if (!map.containsKey(o)) {
				map.put(o, map.size());
			}
			return map.get(o).toString();
		}
		
		@Override
		public String toString() {
			ArrayList<Entry<Object,Integer>> entries = new ArrayList<Entry<Object,Integer>>(map.entrySet());
			Collections.sort(entries, new Comparator<Entry<Object,Integer>>() {
				public int compare(Entry<Object, Integer> o1, Entry<Object, Integer> o2) {
					return o1.getValue().compareTo(o2.getValue());
				}
			});
			StringBuffer sb = new StringBuffer();
			for (Entry<Object,Integer> e : entries) {
				sb.append("\t");
				sb.append(e.getValue());
				sb.append("\t");
				sb.append(e.getKey());
				sb.append("\n");
			}
			
			return sb.toString();
		}

		@Override
		public boolean isEmpty() {
			return map.isEmpty();
		}

	}
	
	
	/**
	 * writes a collection of graphs to a new data set (and finishes it)
	 * @param ags the graphs to be saved in the data set
	 * @param dsName the name of the data set
	 * @throws IOException
	 */
	public void write(Collection<AttributedGraph> ags, String dsName) throws IOException {
		write(ags,dsName,"");
	}
	
	
	/**
	 * writes a collection of graphs to a new data set (and finishes it) at the location specified in path
	 * @param ags the graphs to be saved in the data set
	 * @param dsName the name of the data set
	 * @param path the path where the data set will be saved
	 * @throws IOException
	 */
	public void write(Collection<AttributedGraph> ags, String dsName , String path) throws IOException {
		if(this.adding) //check if there is already a started data set, if so finish it
		{
			this.finishSet();
		}
		else //otherwise start a new one without finishing
		{
			this.finished=true;
		}
		
		this.startNewSetWithNewLabels(path, dsName);
		// create directory
		if(path!="")
		{
			File dir = new File(path+"/"+dsName);
			dir.mkdir();
		}
		else
		{
			File dir = new File(dsName);
			dir.mkdir();
		}
		for(AttributedGraph g: ags)
		{
			this.writeGraph(g);
		}
		this.finishSet();
	}


	private static String obj2String(double[] os) {
		StringBuffer sb = new StringBuffer();
		if (os.length > 0) {
			sb.append(os[0]);
		}
		for (int i=1; i<os.length; i++) {
			sb.append(", ");
			sb.append(String.valueOf(os[i]));
		}
		return sb.toString();
	}

}
