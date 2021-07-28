package comparison.distance;
import java.util.HashSet;

import datastructure.SparseFeatureVector;

public class ManhattanDistance implements Distance<double[]> {

	@Override
	public double compute(double[] vec1, double[] vec2) {
		double result=0;
		//TODO: make sure both vectors are the same length
		for(int i= 0; i< vec1.length; i++)
		{
			result += Math.abs(vec1[i]-vec2[i]);
		}
		
		return result;
	}
	
	public double compute(SparseFeatureVector<Integer> vec1, SparseFeatureVector<Integer> vec2) {
		double result=0;
		//TODO: make sure both vectors are the same length
		//get all non zero entries
		
		HashSet<Integer> keys = new HashSet<Integer>();
		for(Integer i : vec1.nonZeroFeatures())
		{
			keys.add(i);
		}
		for(Integer i : vec2.nonZeroFeatures())
		{
			keys.add(i);
		}
		
		for(Integer key : keys)
		{
			result += Math.abs(vec1.getValue(key)-vec2.getValue(key));
		}
		return result;
	}

}
