package de.codecentric.limiter.api;

import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

public enum BufferError implements ErrorTypeDefinition<BufferError> {
    OVERFLOW
}
