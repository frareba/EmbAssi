package io;

import java.io.IOException;

import benchmark.dataset.AttrDataset;
import benchmark.dataset.LGDataset;
import graph.attributes.AttributedGraph;

public class GetAttrDataset {
	/**
	 * A function for getting an AttrDataset easily. Uses AttributedGraphSetReader.
	 * @param path path, the dataset is saved in
	 * @param dsName name of the dataset
	 * @return an AttrDataset generated from the LGDataset<AttributedGraph> the AttributedGraphSetReader read.
	 * @throws IOException
	 */
	public static AttrDataset getAttrDataset(String path, String dsName) throws IOException
	{
		AttrDataset ds = new AttrDataset(dsName);
		AttributedGraphSetReader agr = new AttributedGraphSetReader();
		ds.addAll(agr.read(path+"/"+dsName+"/"+dsName));
		return ds;
	}
	
	/**
	 * A function for getting an AttrDataset easily. Uses AttributedGraphSetReader. Path is set to where graphs are saved.
	 * @param dsName name of the dataset
	 * @return AttrDataset generated from the LGDataset<AttributedGraph> the AttributedGraphSetReader read.
	 * @throws IOException
	 */
	public static AttrDataset getAttrDataset(String dsName) throws IOException
	{
		String path = "/home/bause/Desktop/datasets/";
		return getAttrDataset(path, dsName);
	}
	//TODO: this could be extended to only reading parts of the dataset and so on
}
