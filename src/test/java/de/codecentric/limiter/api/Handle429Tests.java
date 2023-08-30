package de.codecentric.limiter.api;

import static de.codecentric.limiter.internal.TestUtils.assertRange;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.api.message.Message;

import de.codecentric.limiter.TestHttpResponseAttributesFactory;

public class Handle429Tests extends MuleArtifactFunctionalTestCase {

	@Override
	protected String getConfigFile() {
		return "handle-429-tests.xml";
	}
	
	@Before
	public void clearResponseAttributeList() {
		TestHttpResponseAttributesFactory.clear();
	}
	
	// Use different IDs in the test cases, otherwise they can disturb each other!
	
	@Test
	public void messageProcessorReturningNoAttributes() throws Exception {
		Exception exception = flowRunner("test-missing-attributes").runExpectingException();
		assertTrue("MISSING_ATTRIBUTES not found", exception.getCause().getMessage().contains("MISSING_ATTRIBUTES"));
	}

	@Test
	public void messageProcessorReturningUnexpectedAttributes() throws Exception {
		Exception exception = flowRunner("test-unexpected-attributes").runExpectingException();
		assertTrue("UNEXPECTED_ATTRIBUTES_TYPE not found", exception.getCause().getMessage().contains("UNEXPECTED_ATTRIBUTES_TYPE"));
	}

	@Test
	public void errorInScope() throws Exception {
		Exception exception = flowRunner("test-error-in-scope").runExpectingException();
		// APP:BÄM is part of exceptoin.getInfo(), but that's not accessible here due to class loading restrictions.
		assertEquals("An error occurred.", exception.getMessage());
	}

	@Test
	public void notANumberButString() throws Exception {
		TestHttpResponseAttributesFactory.addEntry(429, "retry-after-ms", "100");
		Exception exception = flowRunner("test-wait-time-not-a-number-but-string").runExpectingException();
		assertTrue("INVALID_NUMBER not found", exception.getCause().getMessage().contains("INVALID_NUMBER"));
	}

	@Test
	public void notANumberButObject() throws Exception {
		TestHttpResponseAttributesFactory.addEntry(429, "retry-after-ms", "100");
		Exception exception = flowRunner("test-wait-time-not-a-number-but-object").runExpectingException();
		assertTrue("INVALID_NUMBER not found", exception.getCause().getMessage().contains("INVALID_NUMBER"));
	}

	@Test
	public void noWaitnecessary() throws Exception {
		TestHttpResponseAttributesFactory.addEntry(200);
		Event event = flowRunner("test-no-wait").run();
		@SuppressWarnings("unchecked")
		Map<String, Object> payload = (Map<String, Object>) event.getMessage().getPayload().getValue();
		long a = (long) payload.get("a");
		long b = (long) payload.get("b");
		// a to b should be immediately, but can be delayed because some Mule parts have to be initialized
		assertRange("a -> b", b - a, 0, 90);
	}

	@Test
	public void oneWaitnecessary() throws Exception {
		TestHttpResponseAttributesFactory.addEntry(429, "retry-after-ms", "100");
		TestHttpResponseAttributesFactory.addEntry(200);
		Event event = flowRunner("test-one-wait").run();
		@SuppressWarnings("unchecked")
		Map<String, Object> payload = (Map<String, Object>) event.getMessage().getPayload().getValue();
		long a = (long) payload.get("a");
		long b = (long) payload.get("b");
		// Wait time is 100 ms
		// a to b should be immediately, but can be delayed because some Mule parts have to be initialized
		assertRange("a -> b", b - a, 100, 190);
	}
	
	@Test
	public void retriesExhausted() throws Exception {
		TestHttpResponseAttributesFactory.addEntry(429, "retry-after-ms", "10");
		TestHttpResponseAttributesFactory.addEntry(429, "retry-after-ms", "10");
		Exception exception = flowRunner("test-retries-exhausted").runExpectingException();
		assertTrue("RETRIES_EXHAUSTED not found", exception.getCause().getMessage().contains("RETRIES_EXHAUSTED"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void waitBeforeFirstAccess() throws Exception {
		TestHttpResponseAttributesFactory.addEntry(429, "retry-after-ms", "100");
		TestHttpResponseAttributesFactory.addEntry(429, "retry-after-ms", "120");
		TestHttpResponseAttributesFactory.addEntry(429, "retry-after-ms", "100");
		TestHttpResponseAttributesFactory.addEntry(429, "retry-after-ms", "120");
		TestHttpResponseAttributesFactory.addEntry(200);
		TestHttpResponseAttributesFactory.addEntry(200);
		TestHttpResponseAttributesFactory.addEntry(200);
		TestHttpResponseAttributesFactory.addEntry(200);
		Event event = flowRunner("test-wait-before-first-access").run();
		Map<String, Object> payload = (Map<String, Object>) event.getMessage().getPayload().getValue();
		Map<String, Object>  route0 = (Map<String, Object>) ((Message) payload.get("0")).getPayload().getValue();
		Map<String, Object>  route1 = (Map<String, Object>) ((Message) payload.get("1")).getPayload().getValue();
		
		// Wait time is 100 ms
		long a = (long) route0.get("a");
		long b = (long) route0.get("b");
		long c = (long) route1.get("c");
		long d = (long) route1.get("d");
		
		System.out.println("a -> b " + (b - a));
		System.out.println("c -> d " + (d - c));
		
		assertRange("a -> b", b - a, 200, 490);
		assertRange("c -> d", d - c, 200, 490);
	}
}
