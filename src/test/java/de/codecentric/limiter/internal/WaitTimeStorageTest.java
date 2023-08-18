package de.codecentric.limiter.internal;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WaitTimeStorageTest {
	private WaitTimeStorage storage;
	private long now;
	
	@Before
	public void setUp() {
		storage = new WaitTimeStorage();
		now = System.currentTimeMillis();
	}

	@After
	public void tearDown() {
		storage = null;
	}

	@Test
	public void testStoreAndRetrieveWaitTime() {
		assertFalse(storage.retrieveWaitTime("res1").isPresent());
		storage.storeWaitTime("res1", now + 1000);
		assertEquals(Long.valueOf(now + 1000), storage.retrieveWaitTime("res1").get());
	}

	@Test
	public void testRemoveWaitTime() {
		storage.storeWaitTime("res1", now + 1000);
		storage.removeWaitTime("res1");
		assertFalse(storage.retrieveWaitTime("res1").isPresent());
	}

	@Test
	public void testRemoveNotExistingWaitTime() {
		storage.removeWaitTime("res1");
		assertFalse(storage.retrieveWaitTime("res1").isPresent());
	}

	@Test
	public void testStoreOverwriteAndRetrieveWaitTime() {
		assertFalse(storage.retrieveWaitTime("res1").isPresent());
		storage.storeWaitTime("res1", now + 1000);
		assertEquals(Long.valueOf(now + 1000), storage.retrieveWaitTime("res1").get());
		storage.storeWaitTime("res1", now + 2000);
		assertEquals(Long.valueOf(now + 2000), storage.retrieveWaitTime("res1").get());
	}

	@Test
	public void testStoreTwoResourcesAndRetrieveWaitTime() {
		storage.storeWaitTime("res1", now + 1000);
		storage.storeWaitTime("res2", now + 1000);
		assertEquals(Long.valueOf(now + 1000), storage.retrieveWaitTime("res1").get());
		assertEquals(Long.valueOf(now + 1000), storage.retrieveWaitTime("res2").get());
	}

	@Test
	public void testStoreAndRetrieveWaitTimeWithOutdatedRemoval() {
		assertFalse(storage.retrieveWaitTime("res1").isPresent());
		storage.storeWaitTime("res1", now - 1000);
		assertFalse(storage.retrieveWaitTime("res1").isPresent());
	}
}
