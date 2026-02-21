package de.codecentric.limiter.api;

import org.mule.sdk.api.error.ErrorTypeDefinition;

public enum RateLimiterError implements ErrorTypeDefinition<RateLimiterError> {
    OVERFLOW,
    INVALID_NUMBER,
    RETRIES_EXHAUSTED
}
