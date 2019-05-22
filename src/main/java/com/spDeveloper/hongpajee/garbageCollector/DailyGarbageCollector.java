package com.spDeveloper.hongpajee.garbageCollector;

import java.util.Stack;

import javax.annotation.PostConstruct;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.spDeveloper.hongpajee.garbageCollector.entity.GarbageCollector;
import com.spDeveloper.hongpajee.garbageCollector.entity.GarbageProducer;
import com.spDeveloper.hongpajee.message.entity.content.proxy.HTMLContentVirtualProxy;
import com.spDeveloper.hongpajee.message.entity.content.proxy.TextContentVirtualProxy;

@Service
public class DailyGarbageCollector implements GarbageCollector{

	Stack<GarbageProducer> registered = new Stack<>();

	
	@PostConstruct
	public void init() {
		HTMLContentVirtualProxy.garbageCollector = this;
		TextContentVirtualProxy.garbageCollector = this;
	}

	@Override
	public void register(GarbageProducer producer) {
		registered.push(producer);		
	}

	@Override
	@Scheduled(cron="22 21 6 * * *")
	public void collectAll() {
		// TODO Auto-generated method stub
		while(!registered.isEmpty()) {
			registered.pop().collectGarbage();
		}		
	}
}
