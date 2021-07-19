# Rate-limiter Module

The rate limiter module offers the feature to limit the performance of a flow or can introduce a fixed delay. 


## Rate limit

You have to create a global element, where you define the minimum time between two calls. If your flow is called too often, 
execution of the rate limit component is delayed, so there is always at least the configured intervall between two calls.

Several components can be linked to one configuration element. The components linked to one global element share the rate
limit.

If you want to create more "channels" which are limitet (with the same or other parameters), you have to create more global
elements.

## Fixed delay

A fixed delay does not need a configuration, just drag the "Fixed delay" component into your flow, set delay and unit.
The flow will be delayed for the configured time.

## Maven dependency

Add this dependency to your application pom.xml

```
		<dependency>
			<groupId>de.codecentric.mule.modules</groupId>
			<artifactId>rate-limiter</artifactId>
			<version>1.0.0-SNAHPSHOT</version>
			<classifier>mule-plugin</classifier>
		</dependency>
```

## Hint for developers

Must be compiled with a JDK 1.8, otherwise tests will not run. You can use the JDK bundled with AnypointStudio, e.g. in:

C:/AnypointStudio-7.9.0/plugins/org.mule.tooling.jdk.v8.win32.x86_64_1.1.1

