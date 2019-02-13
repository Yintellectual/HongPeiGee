package com.spDeveloper.hongpajee.util.map;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class AccumulatorMapTest {

	
	@Test
	//run 15 threads at the same time, each randomly accumulate one of 10 keys for 100 times
	//see if the total accumulation equals 15*100 = 1500
	public void accumulationThreadSafeTest() throws InterruptedException {
		AccumulatorMap accumulatorMap = new AccumulatorMap();
		
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		
		class Accumulator implements Runnable{
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//generate a key from 0 to 9
				String key = ""+ThreadLocalRandom.current().nextInt(10);
				for(int i=0;i<100;i++) {
					accumulatorMap.incrementAndGet(key);
				}
			}
		}
		
		for(int i=0;i<15;i++) {
			executorService.execute(new Accumulator());
		}
		
		executorService.shutdown();
		executorService.awaitTermination(1, TimeUnit.MINUTES);
		
		long sum = 0;
		for(int i=0;i<10;i++) {
			String key = ""+i;
			sum+= accumulatorMap.get(key);
		}
		assertEquals(1500l, sum);
	}
	
	@Test
	public void accumulationToMapTest() {
		AccumulatorMap accumulatorMap = new AccumulatorMap();
		accumulatorMap.incrementAndGet("ddd");
		accumulatorMap.incrementAndGet("ddd");
		accumulatorMap.incrementAndGet("ddd");
		accumulatorMap.incrementAndGet("ddd");
		assertTrue(accumulatorMap.toMap().get("ddd").equals(new Long(4)));
	}
	
	@Test
	public void accumulationFromMapTest() {
		AccumulatorMap accumulatorMap = new AccumulatorMap();
		accumulatorMap.fromMap(Collections.singletonMap("ddd", new Long(4)));
		assertEquals(4, accumulatorMap.get("ddd").longValue());
	}
}
