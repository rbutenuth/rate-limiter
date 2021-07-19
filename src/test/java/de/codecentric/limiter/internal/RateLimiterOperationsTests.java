package de.codecentric.limiter.internal;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.api.event.Event;

public class RateLimiterOperationsTests extends MuleArtifactFunctionalTestCase {

	@Override
	protected String getConfigFile() {
		return "tests.xml";
	}
	
	@Test
	public void rateLimit() throws Exception {
		Event event = flowRunner("test-rate-limit").run();
		@SuppressWarnings("unchecked")
		Map<String, Object> payload = (Map<String, Object>) event.getMessage().getPayload().getValue();
		long a = (long) payload.get("a");
		long b = (long) payload.get("b");
		long c = (long) payload.get("c");
		long d = (long) payload.get("d");
		// a to b should be immediately, but can be delayed because some Mule parts have to be initialized
		assertRange("a - b", b - a, 0, 90);
		assertRange("b - c", c - b, 100, 120);
		assertRange("c - d", d - c, 100, 120);
	}

	@Test
	public void fixedDelay() throws Exception {
		Event event = flowRunner("test-delay").run();
		@SuppressWarnings("unchecked")
		Map<String, Object> payload = (Map<String, Object>) event.getMessage().getPayload().getValue();
		long a = (long) payload.get("a");
		long b = (long) payload.get("b");
		assertRange("a - b", b - a, 100, 190);
	}

	private void assertRange(String message, long value, long lowerBound, long upperBound) {
		assertTrue(message + ", value: " + value + ", lowerBound: " + lowerBound + ", upperbound: " + upperBound, value >= lowerBound);
		assertTrue(message + ", value: " + value + ", lowerBound: " + lowerBound + ", upperbound: " + upperBound, value <= upperBound);
	}
}
