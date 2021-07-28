package comparison.distance.graph.edit.bounds;

/**
 * Flags exact graph edit distances.
 * 
 * @author kriege
 *
 * @param <V>
 * @param <E>
 */
public interface GraphEditDistanceExact<V,E> extends GraphEditDistanceLowerBound<V,E>, GraphEditDistanceUpperBound<V,E> {

}
