# Rate-limiter Module

The rate limiter module offers the feature to limit the performance of a flow or can introduce a fixed delay. 
It has been developed with the support of [C&A](https://www.c-and-a.com/).

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


## Fixed delay

The fixed delay component is used to delay a flow for a certain amount of time. It does not need a global configuration,
just configure the time in the component. It can be used to simulate (mock) another element in benchmark scenarios.


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

## Release notes

* 1.0.0 2021-07-19: Initial release available on Maven central

## Hint for developers

Must be compiled with a JDK 1.8, otherwise tests will not run. You can use the JDK bundled with AnypointStudio, e.g. in:

C:/AnypointStudio-7.9.0/plugins/org.mule.tooling.jdk.v8.win32.x86_64_1.1.1

