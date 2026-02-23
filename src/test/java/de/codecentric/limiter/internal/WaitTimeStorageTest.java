package de.codecentric.limiter.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WaitTimeStorageTest {
	private WaitTimeStorage storage;
	
	@BeforeEach
	public void setUp() {
		storage = new WaitTimeStorage();
	}

	@AfterEach
	public void tearDown() {
		storage = null;
	}

	@Test
	public void testStoreAndRetrieveWaitTime() {
		assertFalse(storage.retrieveWaitTime("res1").isPresent());
		storage.storeWaitTime("res1", System.currentTimeMillis() + 1000);
		assertEquals(Long.valueOf(System.currentTimeMillis() + 1000), storage.retrieveWaitTime("res1").get());
	}

	@Test
	public void testRemoveWaitTime() {
		storage.storeWaitTime("res1", System.currentTimeMillis() + 1000);
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
		storage.storeWaitTime("res1", System.currentTimeMillis() + 1000);
		assertEquals(Long.valueOf(System.currentTimeMillis() + 1000), storage.retrieveWaitTime("res1").get());
		storage.storeWaitTime("res1", System.currentTimeMillis() + 2000);
		assertEquals(Long.valueOf(System.currentTimeMillis() + 2000), storage.retrieveWaitTime("res1").get());
	}

	@Test
	public void testStoreTwoResourcesAndRetrieveWaitTime() {
		storage.storeWaitTime("res1", System.currentTimeMillis() + 1000);
		storage.storeWaitTime("res2", System.currentTimeMillis() + 1000);
		assertEquals(Long.valueOf(System.currentTimeMillis() + 1000), storage.retrieveWaitTime("res1").get());
		assertEquals(Long.valueOf(System.currentTimeMillis() + 1000), storage.retrieveWaitTime("res2").get());
	}

	@Test
	public void testStoreAndRetrieveWaitTimeWithOutdatedRemoval() {
		assertFalse(storage.retrieveWaitTime("res1").isPresent());
		storage.storeWaitTime("res1", System.currentTimeMillis() - 1000);
		assertFalse(storage.retrieveWaitTime("res1").isPresent());
	}
	
	@Test
	public void testRetrieveOfExpiredIdShouldReturnNotPresent() throws Exception {
		long now = System.currentTimeMillis();
		storage.storeWaitTime("res1", now + 50);
		storage.storeWaitTime("res2", now + 50);
		assertTrue(storage.retrieveWaitTime("res1").isPresent());
		assertTrue(storage.retrieveWaitTime("res2").isPresent());
		storage.removeWaitTime("res2");
		assertFalse(storage.retrieveWaitTime("res2").isPresent());
		Thread.sleep(100);
		assertFalse(storage.retrieveWaitTime("res1").isPresent());
	}
	
}
