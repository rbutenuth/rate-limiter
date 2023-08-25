package de.codecentric.limiter.internal;

import static org.junit.Assert.assertTrue;

public class TestUtils {
	public static void assertRange(String message, long value, long lowerBound, long upperBound) {
		assertTrue(message + ", value: " + value + ", lowerBound: " + lowerBound + ", upperbound: " + upperBound, value >= lowerBound);
		assertTrue(message + ", value: " + value + ", lowerBound: " + lowerBound + ", upperbound: " + upperBound, value <= upperBound);
	}

}
