package de.codecentric.limiter.internal;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUtils {
	public static void assertRange(String message, long value, long lowerBound, long upperBound) {
		assertTrue(value >= lowerBound, message + ", value: " + value + ", lowerBound: " + lowerBound + ", upperbound: " + upperBound);
		assertTrue(value <= upperBound, message + ", value: " + value + ", lowerBound: " + lowerBound + ", upperbound: " + upperBound);
	}

}
