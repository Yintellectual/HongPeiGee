package com.spDeveloper.hongpajee.util.map;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.sun.xml.bind.v2.schemagen.xmlschema.List;

//this class should be thread safe. 
public class AccumulatorMap {

	Map<String, AtomicLong> storage = new ConcurrentHashMap<>();

	public Long getAndIncrement(String key) {
		introduceKey(key);
		AtomicLong value = storage.get(key);
		return value.getAndIncrement();
	}

	public Long incrementAndGet(String key) {
		introduceKey(key);
		AtomicLong value = storage.get(key);
		return value.incrementAndGet();
	}

	public Long get(String key) {
		introduceKey(key);
		AtomicLong value = storage.get(key);
		return value.get();
	}

	public void removeKey(String key) {
		storage.remove(key);
	}

	private void introduceKey(String key) {
		storage.putIfAbsent(key, new AtomicLong());
	}

	public Map<String, Long> toMap() {
		Map<String, Long> result = new HashMap<>();
		storage.keySet().stream().forEach(key->{
			AtomicLong value = storage.get(key);
			if(value==null) {
				//do nothing
			}else {
				Long count = value.get();
				result.put(key, count);
			}
		});
		return result;
	}
	public void fromMap(Map<String, Long> map) {
		map.keySet().stream().forEach(key->{
			Long value = map.get(key);
			if(value!=null) {
				storage.put(key, new AtomicLong(value));
			}
		});
	}
}
