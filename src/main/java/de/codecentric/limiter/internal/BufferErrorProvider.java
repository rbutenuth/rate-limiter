package de.codecentric.limiter.internal;

import de.codecentric.limiter.api.RateLimiterError;
import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.HashSet;
import java.util.Set;

public class BufferErrorProvider implements ErrorTypeProvider {

    @SuppressWarnings("rawtypes")
	@Override
    public Set<ErrorTypeDefinition> getErrorTypes() {
        Set<ErrorTypeDefinition> errorTypeDefinitions = new HashSet<>();
        errorTypeDefinitions.add(RateLimiterError.OVERFLOW);
        return errorTypeDefinitions;
    }
}
