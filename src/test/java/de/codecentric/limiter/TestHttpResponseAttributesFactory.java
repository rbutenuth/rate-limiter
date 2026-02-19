package de.codecentric.limiter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestHttpResponseAttributesFactory {
	private static List<TestHttpResponseAttributes> attributeList = new ArrayList<>();
	
	public static synchronized void clear() {
		attributeList = new ArrayList<>();
	}
	
	public static synchronized void addEntry(int statusCode, String... headers) {
		if (headers.length % 2 != 0) {
			throw new IllegalArgumentException("Number of headers must be even (key value pairs)");
		}
		Map<String, String> headerMap = new HashMap<>();
		for (int i = 0; i < headers.length; i += 2)
			headerMap.put(headers[i], headers[i+1]);
		
		attributeList.add(new TestHttpResponseAttributes(statusCode, headerMap));
	}

	public static synchronized TestHttpResponseAttributes fetchAttributes() {
		return attributeList.remove(0);
	}
}
