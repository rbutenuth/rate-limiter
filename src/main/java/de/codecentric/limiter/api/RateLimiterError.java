package de.codecentric.limiter.api;

import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

public enum RateLimiterError implements ErrorTypeDefinition<RateLimiterError> {
    OVERFLOW,
    INVALID_NUMBER,
    MISSING_ATTRIBUTES,
    UNEXPECTED_ATTRIBUTES_TYPE,
    RETRIES_EXHAUSTED
}
