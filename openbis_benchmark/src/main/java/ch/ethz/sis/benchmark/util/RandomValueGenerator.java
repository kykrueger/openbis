package ch.ethz.sis.benchmark.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class RandomValueGenerator<V> {
	public List<V> valuesAsList = new ArrayList<>();
	public Set<V> valuesAsMap = new HashSet<>();
	
	public void add(V value) {
		if(!valuesAsMap.contains(value)) {
			valuesAsMap.add(value);
			valuesAsList.add(value);
		}
	}
	
	public boolean contains(V value) {
		return valuesAsMap.contains(value);
	}
	
	public V getRandom() {
		return valuesAsList.get(ThreadLocalRandom.current().nextInt(0, valuesAsList.size()));
	}
	
}
