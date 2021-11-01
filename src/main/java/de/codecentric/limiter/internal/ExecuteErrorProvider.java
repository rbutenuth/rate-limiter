package de.codecentric.limiter.internal;

import de.codecentric.limiter.api.BufferError;
import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.HashSet;
import java.util.Set;

public class ExecuteErrorProvider implements ErrorTypeProvider {

    @Override
    public Set<ErrorTypeDefinition> getErrorTypes() {
        Set<ErrorTypeDefinition> errorTypeDefinitions = new HashSet<>();
        errorTypeDefinitions.add(BufferError.OVERFLOW);
        return errorTypeDefinitions;
    }
}
