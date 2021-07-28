package graph.properties;

import graph.Graph;

public abstract class GraphPropertyAdapter<E extends Graph.Element,T> implements GraphProperty<E,T> {
	
	public void set(E k, T v) {
		throw new UnsupportedOperationException();
	};
	
	public T get(E e) {
		throw new UnsupportedOperationException();
	};
}
