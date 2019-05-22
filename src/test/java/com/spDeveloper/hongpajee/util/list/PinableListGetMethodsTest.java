package com.spDeveloper.hongpajee.util.list;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

public class PinableListGetMethodsTest {
	
	PinableList pinableList = null;
	
	@Before
	public void prepareData() {
		pinableList = new PinableList();
		for(int i=0;i<5;i++) {
			pinableList.pinToLast("pin"+i);
			pinableList.unpinToLast("unpin"+i);
		}
	}
	
	@Test
	public void getAll_return_getPined_before_getUnpined() throws InterruptedException, TimeoutException {

		List<String> strings = pinableList.getAll();
		for(int i=5;i<10;i++) {
			assertTrue("getAll():show pined elements before unpined", strings.get(i).contains("unpin"));
		}
	}	

	@Test
	public void can_pin_to_first() throws InterruptedException, TimeoutException {
		pinableList.pinToFirst("pin_to_first");
		assertEquals("pin_to_first", pinableList.getPined().get(0));
	}
	
	@Test
	public void can_pin_to_last() throws InterruptedException, TimeoutException {
		pinableList.pinToLast("pin_to_last");
		assertEquals("pin_to_last", pinableList.getPined().get(5));
	}
	
	@Test
	public void can_pin_unpined_element() throws InterruptedException, TimeoutException {
		pinableList.pinToFirst("unpin0");
		assertTrue("unpin0 is pined.", !pinableList.getUnpined().contains("unpin0"));
		assertTrue("unpin0 is pined.", pinableList.getPined().contains("unpin0"));
	}
	
	@Test
	public void unpin_add_non_member_to_unpined_list() throws InterruptedException, TimeoutException {
		pinableList.unpinToFirst("non-member");
		assertTrue(pinableList.isUnpined("non-member"));
	}
	
	@Test
	public void unpin_does_nothing_to_unpined() throws InterruptedException, TimeoutException {
		pinableList.unpinToFirst("unpin0");
		assertTrue(pinableList.isUnpined("unpin0"));
	}
	
	@Test
	public void can_unpin_pinned() throws InterruptedException, TimeoutException {
		pinableList.unpinToFirst("pin0");
		assertTrue(pinableList.isUnpined("pin0"));
		assertTrue(!pinableList.isPined("pin0"));
	}
	
	@Test
	public void can_remove_pined() throws InterruptedException, TimeoutException {
		pinableList.remove("pin0");
		assertTrue(!pinableList.contains("pin0"));
	}
	@Test
	public void can_remove_unpined() throws InterruptedException, TimeoutException {
		pinableList.remove("unpin0");
		assertTrue(!pinableList.contains("unpin0"));
	}
	@Test
	public void remove_does_nothing_to_non_member() throws InterruptedException, TimeoutException {
		pinableList.remove("non-member");
		assertTrue(!pinableList.contains("non-member"));
	}
	
}
