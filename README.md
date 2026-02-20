# Rate-limiter Module

The rate limiter module offers the feature to limit the performance of a flow or can introduce a fixed delay. 
It has been developed by [C&A](https://www.c-and-a.com/) and [codecentric](https://www.codecentric.de/).

Compared to a simple Thread.sleep() it has the advantage to hook into the asynchronous event mechanism of the 
Mule runtime: During wait, no thread is blocked. The behavior is similar to modules with an asynchronous implementation,
e.g. http: While the request is handled by the remote system, no thread on the client side is blocked. 


## Rate limit

A rate limit component is used to limit the number of executions in a certain amount of time. It is configured by setting
the time between two executions. For example, a time of 200 milliseconds yields a maximum frequency of 1/0.2 s = 5 Hz.

If the rate limit component is called the next time before the configured time has been elapsed, the component will wait.

You have to create a global element, where you define the minimum time between two calls. 
Several components can be linked to one global element. The components linked to the same global element share the rate
limit.

If you want to create several "channels" which are limited (with the same or other parameters), 
you have to create several global elements.

### Unbounded & Bounded Buffer 

If a flow is executed in parallel and the rate limit processor uses the unbounded buffer it can happen that the memory overflows. 
To prevent this the rate limit processor can use a bounded buffer with a maximum size. 
If this size is exceeded, an exception of type Buffer.OVERFLOW is thrown and the unbounded event can be handled in order to give the consumer a quick response.

## Fixed delay

The fixed delay component is used to delay a flow for a certain amount of time. It does not need a global configuration,
just configure the time in the component. It can be used to simulate (mock) another element in benchmark scenarios.


## Handle 429

A scope in which you can place an HTTP request to a rate limited resource. When the resource follows the HTTP
RFC, it will answer with status code 429 and a header "retry-after" with a wait time in seconds (or a time
when you are allowed to make the next request). The scope evaluates both values. When the wait response code
is returned, it will wait up to the specified point in time and try again. This wait is associated with an ID:
Other occurrences of this scope in the same application with the same ID will not start the first call, but
"join" the waiting room to the point in time. For these "joins", you can specify an additional
wait time. This avoids all waiting operations fire up at exactly the same time, overloading your own application and
the target server.

The times returned by the DataWeave expressions are all in milliseconds. The default values are:   

* Wait time expression: #[((headers."retry-after" default "0" as Number) + random() * 100) * 1000]
  Wait the specified time, add - randomly - 0 to 100 seconds.
* Join wait time expression: #[100 + random() * 1000]
  Wait additional 100 milliseconds plus 0 to 1 second.

*Note:* The default of the HTTP requestor is to throw an error when the server returns a 429 status code,
so you have to configure a response validator to accept 429. 

*Note for testing*: The scope evaluates the expressions `attributes.statusCode` and `attributes.headers`. 
In case the `attributes` are missing completely (e.g. in an MUnit test), the scopes handles it as success.
When you inject your own `attributes` (e.g. in a Mock), make sure to use mime type `application/java`.
In case you don't inject `attributes`, be careful that they are really empty, and you don't have some stray
`attributes` from previous message processors or the source in you event.

Here a complete example:

```
<rate-limiter:handle-429
	id="ping-resource" numberOfRetries="5" waitStatusCode="429" 
	waitTimeExpression='#[(headers."retry-after" default "0" as Number) * 1000]' 
	joinWaitTimeExpression="#[1000]">
	<http:request method="GET" doc:name="/ping" config-ref="ping-config" path="/ping" >
		<http:response-validator >
			<http:success-status-code-validator values="100..399,429" />
		</http:response-validator>
	</http:request>
</rate-limiter:handle-429>
```


## Maven dependency

Add this dependency to your application pom.xml

```
<dependency>
    <groupId>io.github.rbutenuth.mule-modules</groupId>
    <artifactId>rate-limiter</artifactId>
    <version>1.2.3</version>
	<classifier>mule-plugin</classifier>
</dependency>
```

## Release notes

* 1.2.3 2026-02-20: Changed Maven coordinates, updated dependencies, minimum required Mule version now 4.9.0 and Java 17.
* 1.2.2 2026-02-19: skipped
* 1.2.1 2024-08-29: Minimum required Mule version now 4.6.0 (was 4.7.0)
* 1.2.0 2024-08-28: handle-429 scope handles missing attributes (e.g. in MUnit test) as success. 
                    Additionally, you can provide attributes as Map, not only as Java class, as before.
* 1.1.0 2024-07-18: Updated dependencies, can now run in a Mule server running with JDK 17.
* 1.0.7 2024-02-26: Reduced thread pool size. (Pool is only needed to schedule to the next uber thread, so it can be small.)
* 1.0.6 2023-08-31: Improved handler for 429: More options, join wait when one is active
* 1.0.5 2023-08-27: Added handler for 429 HTTP response handling (client side handling of rate limiting)
* 1.0.4 2021-11-03: Added bounded buffer (thanks to Benjamin Lüdicke for the pull request)
* 1.0.0 2021-07-19: Initial release available on Maven central

## Hint for developers

Must be compiled with a JDK 1.8, otherwise tests will not run (missing package in newer JDKs). This is a restriction of the Mule SDK. 

You can use the JDK bundled with AnypointStudio, e.g. in: C:/AnypointStudio-7.15.0/plugins/org.mule.tooling.jdk.v8.win32.x86_64_1.1.1

