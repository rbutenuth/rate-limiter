package de.codecentric.limiter.api;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.api.event.Event;

import de.codecentric.limiter.TestHttpResponseAttributesFactory;

public class Handle429Tests extends MuleArtifactFunctionalTestCase {

	@Override
	protected String getConfigFile() {
		return "handle-429-tests.xml";
	}

	@Test
	public void messageProcessorReturningNoAttributes() throws Exception {
		Exception exception = flowRunner("test-missing-attributes").runExpectingException();
		assertTrue("MISSING_ATTRIBUTES not found", exception.getCause().getMessage().contains("MISSING_ATTRIBUTES"));
	}

	@Test
	public void noWaitnecessary() throws Exception {
		TestHttpResponseAttributesFactory.addEntry(200);
		Event event = flowRunner("test-no-wait").run();
		@SuppressWarnings("unchecked")
		Object payload = event.getMessage().getPayload().getValue();
		System.out.println(payload);
	}
}
