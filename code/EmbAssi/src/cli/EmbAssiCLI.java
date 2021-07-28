package cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;

import benchmark.dataset.AttrDataset;
import comparison.distance.graph.edit.tools.GenerateDataset;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class EmbAssiCLI {
	static class CommandMain extends KCommon.CommandMain {

		@Parameter(names = { "-D","--datadir" }, description = "Directory containing the data files", converter = FileConverter.class)
		File dataDir = new File("data");

		@Parameter(names = { "-G","--geddir" }, description = "Directory where the output is stored", converter = FileConverter.class)
		File gedDir = new File(".");

		@Parameter(names = { "-d", "--dataset" }, description = "Name of dataset")
		String dataset;
		
		@Parameter(names = { "-knn", "--knnSearch" }, description = "Change type of query to knn")
		boolean knn;
		
		@Parameter(names = { "-cA", "--compareApprox" }, description = "Compare different approximations (Gurobi is needed for this)")
		boolean comppareApprox;
		
		@Parameter(names = { "-qr", "--queryRange" }, description = "Maximum Range or k, depending on the query type")
		int queryR = 5;
		
		@Parameter(names = { "-qn", "--queryNumber" }, description = "Number of queries")
		int queryN = 50;
		
		@Parameter(names = { "-clb", "--clb" }, description = "Use CLB in a prefiltering step")
		boolean clb;
		
		@Parameter(names = { "-m", "--method" }, description = "Method to be used")
		String method;
		
		@Parameter(names = { "-r", "--raw" }, description = "Does the dataset have disconnected graphs, edge labels or any attributes?")
		boolean raw;
		
		

	}

	static CommandMain cm = new CommandMain();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] argsString) throws IOException, InterruptedException {

		JCommander jc = new JCommander(cm);
		jc.getMainParameter();
		jc.setProgramName("EmbAssi");
		jc.parse(argsString);

		if (cm.help || cm.dataset == null && cm.method == null) {
			jc.usage();
			System.exit(0);
		}

		if (cm.dataset == null) {
			throw new ParameterException("No dataset specified.");
		}
		if (cm.method == null) {
			throw new ParameterException("No method specified.");
		}
		if (cm.knn && cm.clb) {
			if(!cm.method.equals("CLB"))
			{
				System.err.println("Acceleration of knn queries using CLB and another method is currently not implemented.");
				System.err.println("Will perform knn search with only CLB vs "+ cm.method + " instead.");
			}
		}
		
		AttrDataset ds = KCommon.load(cm.dataset, cm.dataDir);
		if(cm.raw)
		{
			GenerateDataset.generateConnectedDataset(ds, cm.dataDir.getAbsolutePath(),cm.dataset+"_p",false);
			ds = KCommon.load(cm.dataset+"_p", cm.dataDir);
		}

		String fileName = cm.gedDir.getAbsolutePath() + "/" + "results.txt";
		BufferedWriter tw = new BufferedWriter( new FileWriter(fileName, true));
		if(cm.comppareApprox)
		{
			System.out.println("Comparing different approximations");
			tw.append("Comparing different approximations");
			tw.newLine();
			System.out.println("Dataset:  " + ds.getID());
			tw.append("Dataset:  " + ds.getID());
			tw.newLine();
			tw.close();
			ExperimentsEmbAssi.plotapprox(ds, cm.queryN, fileName);
			
			System.exit(0);
		}
		if(cm.clb)
		{
			System.out.println("Method:   CLB/" + cm.method);
			tw.append("Method:   CLB/" + cm.method);
			tw.newLine();
		}
		else
		{
			System.out.println("Method:   " + cm.method);
			tw.append("Method:   " + cm.method);
			tw.newLine();
		}

		System.out.println("Dataset:  " + ds.getID());
		tw.append("Dataset:  " + ds.getID());
		tw.newLine();
		if (cm.knn)
		{
			if(cm.queryN>20)
			{
				System.err.println("Setting query number to 20, since "+ cm.queryN +" is taking too long for knn search.");
				cm.queryN = 20;
				
			}
			System.out.println("Query type:   KNN");
			tw.append("Query type:   KNN");
			tw.newLine();
		}
		else
		{
			System.out.println("Query type:   Range");
			tw.append("Query type:   Range");
			tw.newLine();
		}
		tw.close();
		//generate random queries
		IntArrayList queries = ExperimentsEmbAssi.generateNumbers(cm.queryN, ds.size());
		
		//preprocessing
		ExperimentsEmbAssi e = new ExperimentsEmbAssi(cm.method, cm.clb,ds, cm.knn, fileName);
		
		//queries 1 - max threshold
		e.run(queries, cm.queryR);
		
		
		System.out.println();
	}
	
}
