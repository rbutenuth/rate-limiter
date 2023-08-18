package de.codecentric.limiter.api;

import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.SubTypeMapping;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.runtime.extension.api.annotation.error.ErrorTypes;

/**
 * This is the main class of an extension, is the entry point from which configurations, connection providers, operations
 * and sources are going to be declared.
 */
@Xml(prefix = "rate-limiter")
@Extension(name = "Rate-limiter")
@SubTypeMapping(baseType = BufferOps.class, subTypes = {UnboundedBuffer.class, BoundedBuffer.class})
@ErrorTypes(BufferError.class)
@Configurations(RatelimiterConfiguration.class)
public class RatelimiterExtension {
	//
}
