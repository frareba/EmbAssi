package benchmark.dataset;

import java.util.HashMap;

// TODO prototype implementation; do not delete from this dataset
public class PointDataset extends Dataset<double[]> {
    HashMap<Object,String> classLabels;

	public PointDataset(String id) {
		super(id);
		classLabels = new HashMap<Object, String>();
	}

	@Override
	public String getClassLabel(double[] g) {
		return classLabels.get(g);
	}
	
	public void add(double[] point, String label) {
		add(point);
		classLabels.put(point, label);
	}
	
	public int dim() {
		return get(0).length;
	}
	
	/**
	 * Normalizes each dimension by linear scaling.
	 */
	public PointDataset normalized() {
		PointDataset r = new PointDataset(this.getID()+"norm");
		//determine min and max for each dimension
		double[] min = new double[dim()];
		double[] max = new double[dim()];
		for (int i=0; i<dim(); i++) {
			min[i] = Double.MAX_VALUE;
			max[i] = Double.MIN_VALUE;
		}
		for (int j=0; j<this.size(); j++) {
			double[] p = this.get(j);
			for (int i=0; i<dim(); i++) {
				min[i] = Math.min(min[i], p[i]);
				max[i] = Math.max(max[i], p[i]);
			}
		}
//		for (int i=0; i<dim(); i++) {
//			System.out.println(min[i]);
//			System.out.println(max[i]);
//		}
		for (int j=0; j<this.size(); j++) {
			double[] p = this.get(j);
			double[] newP = new double[dim()];
			for (int i=0; i<dim(); i++) {
				newP[i] = p[i];
				newP[i] -= min[i]; // make positive
				newP[i] /= (max[i] - min[i]); // divide by range
			}
			r.add(newP, getClassLabel(p));
		}
		 		
		return r;		
	}

	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		for (double dp[] : this) {
			for (double d : dp) {
				sb.append(d);
				sb.append(' ');
			}
			sb.append(getClassLabel(dp));
			sb.append('\n');
		}
		
		return sb.toString();
	}

	@Override
	public Dataset<double[]> newEmptyInstance(String id) {
		return new PointDataset(id);
	}

}
