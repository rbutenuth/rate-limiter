package de.codecentric.limiter.api;

import static org.junit.Assert.assertTrue;
import static de.codecentric.limiter.internal.TestUtils.assertRange;

import java.util.Map;

import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.api.event.Event;

public class RateLimitAndFixedDelayTests extends MuleArtifactFunctionalTestCase {

	@Override
	protected String getConfigFile() {
		return "rate-limit-tests.xml";
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
		assertRange("b - c", c - b, 90, 140);
		assertRange("c - d", d - c, 90, 140);
	}

	@Test
	public void rateLimitBoundedBufferOverflow() throws Exception {
		Exception exception = flowRunner("test-ratelimit-overflow-error").runExpectingException();
		assertTrue("Buffer.Overflow not found", exception.getCause().getMessage().contains("Maximum buffer size exceeded"));
	}

	@Test
	public void fixedDelay() throws Exception {
		Event event = flowRunner("test-delay").run();
		@SuppressWarnings("unchecked")
		Map<String, Object> payload = (Map<String, Object>) event.getMessage().getPayload().getValue();
		long a = (long) payload.get("a");
		long b = (long) payload.get("b");
		assertRange("a - b", b - a, 90, 190);
	}
}
