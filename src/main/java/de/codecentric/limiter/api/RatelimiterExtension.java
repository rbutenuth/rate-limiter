package de.codecentric.limiter.api;

import static org.mule.sdk.api.meta.JavaVersion.*;

import org.mule.sdk.api.annotation.Configurations;
import org.mule.sdk.api.annotation.Extension;
import org.mule.sdk.api.annotation.JavaVersionSupport;
import org.mule.sdk.api.annotation.SubTypeMapping;
import org.mule.sdk.api.annotation.dsl.xml.Xml;
import org.mule.sdk.api.annotation.error.ErrorTypes;

/**
 * This is the main class of an extension, is the entry point from which configurations, connection providers, operations
 * and sources are going to be declared.
 */
@Xml(prefix = "rate-limiter")
@Extension(name = "Rate-limiter")
@SubTypeMapping(baseType = BufferOps.class, subTypes = {UnboundedBuffer.class, BoundedBuffer.class})
@ErrorTypes(RateLimiterError.class)
@Configurations(RatelimiterConfiguration.class)
@JavaVersionSupport({ JAVA_17 })
public class RatelimiterExtension {
	//
}
