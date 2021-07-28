package comparison.distance.graph.edit.vectordistance;

import java.util.ArrayList;
import benchmark.dataset.AttrDataset;
import comparison.distance.Distance;
import comparison.distance.graph.edit.bounds.GraphEditDistanceBLPF2lb;
import comparison.distance.graph.edit.bounds.LabelCount;
import comparison.distance.graph.edit.bounds.beamsearch.GraphEditBeamSearchDistance;
import comparison.distance.graph.edit.bounds.branch.Branch;
import comparison.distance.graph.edit.bounds.branch.BranchSimple;
import comparison.distance.graph.edit.bounds.branch.BranchUpperBound;
import comparison.distance.graph.edit.bounds.cstar.CStarLowerBound;
import comparison.distance.graph.edit.bounds.cstar.CStarUpperBound;
import comparison.distance.graph.edit.bounds.cstar.CStarUpperBoundRefined;
import comparison.distance.graph.edit.bounds.exact.BSSGraphEditDistance;
import comparison.distance.graph.edit.bounds.exact.GraphEditDistanceBLPF2;
import comparison.distance.graph.edit.treedistance.GraphEditAssignmentCostsTreeDistanceDiscrete;
import comparison.distance.graph.edit.treedistance.GraphEditDistanceAssignmentLinearTreeDistance;
import comparison.distance.graph.edit.vectordistance.distances.GraphEditCostsCombinedVectorDistance;
import comparison.distance.graph.edit.vectordistance.distances.GraphEditCostsEdgeVectorDistance;
import comparison.distance.graph.edit.vectordistance.distances.GraphEditCostsVertexVectorDistance;
import comparison.distance.graph.edit.vectordistance.distances.SimpleGraphEditCosts;
import comparison.distance.graph.edit.vectordistance.treedistances.DegreeLowerBound;
import comparison.distance.graph.edit.vectordistance.treedistances.EdgeLowerBound;
import comparison.distance.graph.edit.vectordistance.treedistances.LabelLowerBound;
import comparison.distance.tree.TreeDistance;
import graph.LGraph;
import graph.Graph.Edge;
import graph.Graph.Vertex;
import graph.attributes.Attributes;

public class DistanceBuilder {
	public static Distance<LGraph<Attributes,Attributes>> getDistance(AttrDataset ds, String dist,SimpleGraphEditCosts<Attributes, Attributes> gec)
	{
		Distance<LGraph<Attributes,Attributes>> distance;
		switch (dist) {
		case "llb":
			//System.out.println("(Vertex) Label Lower Bound");
			TreeDistance<Vertex> llb = new LabelLowerBound(ds, gec);
			distance = new GraphEditCostsVertexVectorDistance(ds,llb);
			break;
		case "dlb":
			//System.out.println("Degree Lower Bound");
			TreeDistance<Vertex> dlb = new DegreeLowerBound(ds, gec);
			distance = new GraphEditCostsVertexVectorDistance(ds,dlb);
			break;
		case "elb":
			//System.out.println("Edge Label Lower Bound");
			TreeDistance<Edge> elb = new EdgeLowerBound(ds,gec);
			distance = new GraphEditCostsEdgeVectorDistance(ds,elb);
			break;
		case "clb":
			//System.out.println("Combined Lower Bound");
			ArrayList<TreeDistance<Vertex>> dists = new ArrayList<TreeDistance<Vertex>>();
			dists.add(new LabelLowerBound(ds, gec));
			dists.add(new DegreeLowerBound(ds, gec));
			distance = new GraphEditCostsCombinedVectorDistance(ds, dists);
			break;
		case "linD":
			//System.out.println("Lineartime Distance (Optimal Assignment) with Weisfeiler-Lehman");
			TreeDistance<Vertex> tdi = new GraphEditAssignmentCostsTreeDistanceDiscrete(1,ds); //TODO: maybe more iterations
			distance = new GraphEditDistanceAssignmentLinearTreeDistance<Attributes,Attributes>(gec, tdi);
			break;
		case "beamD":
			//System.out.println("BeamSearch Distance");
			distance = new GraphEditBeamSearchDistance(gec, 100);
			break;
		case "exact":
			//System.out.println("Exact Distance with Gurobi Solver");
			distance = new GraphEditDistanceBLPF2<>(gec);
			//TODO: for attributed graphs graph edit costs here should not be simple...
			break;
		case "exactST":
			//System.out.println("Exact Distance with Gurobi Solver");
			distance = new GraphEditDistanceBLPF2<>(gec,20);
			break;
		case "exactLT":
			//System.out.println("Exact Distance with Gurobi Solver");
			distance = new GraphEditDistanceBLPF2<>(gec,120);
			break;
		case "slf":
			//System.out.println("Simple Label Filter Lower Bound");
			distance = new LabelCount(gec);
			break;
		case "cstarlb":
			//System.out.println("CStar Lower Bound");
			distance = new CStarLowerBound();
			//TODO: how can you change gec for this?
			break;
		case "cstarub":
			//System.out.println("CStar Upper Bound");
			distance = new CStarUpperBound(gec);
			break;
		case "cstarubref":
			//System.out.println("CStar Upper BoundRefined");
			distance = new CStarUpperBoundRefined(gec);
			break;
		case "branch":
			//System.out.println("Branch (LowerBound)");
			distance = new Branch(gec);
			break;
		case "branchub":
			//System.out.println("Branch (UpperBound)");
			distance = new BranchUpperBound(gec);
			break;
		case "branchSimple":
			//System.out.println("BranchSimple (LowerBound)");
			distance = new BranchSimple(gec);
			break;
		case "bssGED":
			//System.out.println("BSS GED (verifier)");
			distance = new BSSGraphEditDistance(5);
			break;
		case "blplb":
			//System.out.println("BLP F2 without boolean variables");
			distance = new GraphEditDistanceBLPF2lb(gec);
			break;
		default:
			System.out.println("Default: Exact graph edit distance");
			distance = new GraphEditDistanceBLPF2<>(gec);
		}
		
		return distance;
	}
}