package ch.ethz.sis.benchmark.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class RandomValueGenerator<V> {
	public List<V> valuesAsList = new ArrayList<>();
	public Set<V> valuesAsMap = Collections.newSetFromMap(new ConcurrentHashMap<>());
	
	public synchronized void add(V value) {
		if(!valuesAsMap.contains(value)) {
			valuesAsMap.add(value);
			valuesAsList.add(value);
		}
	}
	
	public synchronized void addAll(Collection<V> values) {
		for(V value:values) {
			add(value);
		}
	}
	
	public synchronized boolean contains(V value) {
		return valuesAsMap.contains(value);
	}
	
	public synchronized V getRandom() {
		return valuesAsList.get(ThreadLocalRandom.current().nextInt(0, valuesAsList.size()));
	}
	
}
