package io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import graph.Graph.Edge;
import graph.Graph.Vertex;
import graph.attributes.AttributedGraph;

public class AttributedGraphToMLIndexFormatWriter {

	public static void write(Collection<AttributedGraph> ds, String path, String dsName) throws IOException
	{
		File newFile = new File(path+dsName);
		BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
		
		
		for(AttributedGraph graph: ds)
		{	
			
			// (knoten:) v index label
			for(Vertex v :graph.getGraph().vertices())
			{
				writer.write("v "+v.getIndex() + " ");
				writer.write(graph.getVertexLabel().get(v).getNominalAttributeString()+" ");
				writer.newLine();
			}
			
			for(Edge e :graph.getGraph().edges())
			{
				
				writer.write("e "+e.getFirstVertex().getIndex());
				writer.write(" " +e.getSecondVertex().getIndex());
				writer.write(" 0");
				//writer.write(" " +graph.getEdgeLabel().get(e).getNominalAttributeString());
				writer.newLine();
			}
			writer.write("t ");
			writer.newLine();
			//writer.newLine();
		}
		writer.close();
	}

}
