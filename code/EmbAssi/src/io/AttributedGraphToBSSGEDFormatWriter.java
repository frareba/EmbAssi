package io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import graph.Graph.Edge;
import graph.Graph.Vertex;
import graph.attributes.AttributedGraph;

public class AttributedGraphToBSSGEDFormatWriter {

	public static void write(Collection<AttributedGraph> ds, String path, String dsName) throws IOException
	{
		File newFile = new File(path+dsName);
		BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
		
		int i = 1;
		for(AttributedGraph graph: ds)
		{	
			// id des graphen (haben wohl keine graphlabels??)
			writer.write(i +"");
			writer.newLine();
			writer.write(graph.getGraph().getVertexCount()+" "+ graph.getGraph().getEdgeCount());
			writer.newLine();
			// (knoten:) label(v_i)
			for(Vertex v :graph.getGraph().vertices())
			{
				writer.write(graph.getVertexLabel().get(v).getNominalAttributeString()+" ");
				writer.newLine();
			}
			
			for(Edge e :graph.getGraph().edges())
			{
				
				writer.write(""+e.getFirstVertex().getIndex());
				writer.write(" " +e.getSecondVertex().getIndex());
				writer.write(" " +graph.getEdgeLabel().get(e).getNominalAttributeString());
				writer.newLine();
			}
			//writer.newLine();
			i++;
		}
		writer.close();
	}

}
