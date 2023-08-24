package de.codecentric.limiter;

import java.util.HashMap;
import java.util.Map;

public class TestHttpResponseAttributes {
	private final int statusCode;
	private final Map<String, String> headers;
	
	public TestHttpResponseAttributes(int statusCode, Map<String, String> headers) {
		this.statusCode = statusCode;
		this.headers = new HashMap<>(headers);
	}

	public int getStatusCode() {
		return statusCode;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}
}
