package de.codecentric.limiter.internal;

import de.codecentric.limiter.api.RateLimiterError;

import java.util.HashSet;
import java.util.Set;

import org.mule.sdk.api.annotation.error.ErrorTypeProvider;
import org.mule.sdk.api.error.ErrorTypeDefinition;

public class BufferErrorProvider implements ErrorTypeProvider {

    @SuppressWarnings("rawtypes")
	@Override
    public Set<ErrorTypeDefinition> getErrorTypes() {
        Set<ErrorTypeDefinition> errorTypeDefinitions = new HashSet<>();
        errorTypeDefinitions.add(RateLimiterError.OVERFLOW);
        return errorTypeDefinitions;
    }
}
