package com.spDeveloper.hongpajee.garbageCollector.entity;

public interface GarbageCollector {
	
	void register(GarbageProducer producer);
	void collectAll();
}
