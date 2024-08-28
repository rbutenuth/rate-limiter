package de.codecentric.limiter.api;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.mule.runtime.api.el.BindingContext;
import org.mule.runtime.api.i18n.I18nMessageFactory;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.lifecycle.Stoppable;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.scheduler.SchedulerConfig;
import org.mule.runtime.api.scheduler.SchedulerService;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.parameter.Literal;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.extension.api.runtime.route.Chain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.codecentric.limiter.internal.BufferErrorProvider;
import de.codecentric.limiter.internal.Handle429ErrorProvider;
import de.codecentric.limiter.internal.WaitTimeStorage;

/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class RatelimiterOperations implements Startable, Stoppable {
	private static Logger logger = LoggerFactory.getLogger(RatelimiterOperations.class);

	@Inject
	private SchedulerService schedulerService;

	private ScheduledExecutorService scheduledExecutor;
	
	@Inject
	private ExpressionManager expressionManager;
	
	// This *must* be static, the server creates more than one instance of the RateLimiterOperations class.
	// The natural way would be to move this to a configuration, but scopes can't have a configuration.
	private static WaitTimeStorage waitTimes = new WaitTimeStorage();
	
	@Override
	public void start() {
		SchedulerConfig config = SchedulerConfig.config()
				.withMaxConcurrentTasks(2)
				.withShutdownTimeout(1, TimeUnit.SECONDS)
				.withPrefix("rate-limit")
				.withName("operations");
		scheduledExecutor = schedulerService.customScheduler(config);
	}

	@Override
	public void stop() {
		scheduledExecutor.shutdown();
	}

	/**
	 * Delay the flow if called too fast.
	 */
	@Throws(BufferErrorProvider.class)
	public void limitRate(@Config RatelimiterConfiguration configuration, CompletionCallback<Void, Void> callback) {
		logger.debug("schedule command");
		configuration.schedule(scheduledExecutor, () -> {
			logger.debug("execute command");
			callback.success(Result.<Void, Void>builder().build());
		});
	}
	
	public void fixedDelay(long delay, TimeUnit unit, CompletionCallback<Void, Void> callback) {
		logger.debug("delay: " + delay + ", unit: " + unit);
		scheduledExecutor.schedule(() -> {
			logger.debug("execute delayed command");
			callback.success(Result.<Void, Void>builder().build());
		}, delay, unit);
	}

	@OutputResolver(output = SetAttributesOutputResolver.class)
	public Result<Object, Object> setAttributes(@Optional(defaultValue = "#[payload]") Object payload, @Expression(ExpressionSupport.REQUIRED) Object attributes) {
		return Result.<Object, Object>builder().output(payload).attributes(attributes).build();
	}
	
	@Alias("handle-429")
	@Throws(Handle429ErrorProvider.class)
	@MediaType(value = "*/*")
	public void handleRetryAfter(Chain operations, CompletionCallback<Object, Object> callback, //
			@Summary("Resource ID") String id,
			@Summary("How often shall the operation be retried when the first try failed?") @Optional(defaultValue = "5") int numberOfRetries,
			@Summary("Status code for wait") @Optional(defaultValue = "429") int waitStatusCode,
			@Summary("A DataWeave expression to compute the time to wait (in milliseconds)."
					+ "The following values are available: " + "headers: The HTTP response headers as map"
					+ "retryIndex: Which retry attemt is this (first retry: 1). ") 
				@Optional(defaultValue = "#[(headers.\"retry-after\" default \"0\" as Number + random() * 100) * 1000]") Literal<String> waitTimeExpression,
			@Summary("Additional wait time when joining an already active wait (in milliseconds).")
				@Optional(defaultValue = "#[100 + random() * 1000]") Literal<String> joinWaitTimeExpression) {

		RetryAfterRunner repeatRunner = new RetryAfterRunner(operations, callback, //
				id, numberOfRetries, waitStatusCode, waitTimeExpression.getLiteralValue().get(), joinWaitTimeExpression.getLiteralValue().get());
		repeatRunner.run();
	}
	
	/**
	 * Schedulable execution to run the first and followup calls.
	 */
	public class RetryAfterRunner implements Runnable {
		private Chain operations;
		private CompletionCallback<Object, Object> callback;
		private String id;
		private int numberOfRetries;
		private int waitStatusCode;
		private String waitTimeExpression;
		private String joinWaitTimeExpression;
		private int retryIndex;

		private RetryAfterRunner(Chain operations, CompletionCallback<Object, Object> callback, //
				String id, int numberOfRetries, int waitStatusCode, String waitTimeExpression, String joinWaitTimeExpression) {

			this.operations = operations;
			this.callback = callback;
			this.id = id;
			this.numberOfRetries = numberOfRetries;
			this.waitStatusCode = waitStatusCode;
			this.waitTimeExpression = waitTimeExpression;
			this.joinWaitTimeExpression = joinWaitTimeExpression;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void run() {
			java.util.Optional<Long> waitUntil = waitTimes.retrieveWaitTime(id);
			if (waitUntil.isPresent()) {
				long delay = Math.max(0, waitUntil.get() - System.currentTimeMillis());
				delay += computeAdditionalJoinDelay();
				logger.info("enter running delay: {}", delay);
				scheduledExecutor.schedule(this, delay, TimeUnit.MILLISECONDS);
			} else {
				logger.debug("run, retryIndex: {}", retryIndex);
				
				operations.process(result -> {
					if (result.getAttributes().isPresent()) {
						Object attributes = result.getAttributes().get();
						Class<?> clazz = attributes.getClass();
						try {
							int statusCode = (int) clazz.getMethod("getStatusCode").invoke(attributes);
							logger.debug("status code: {}", statusCode);
							if (statusCode == waitStatusCode) {
								Map<String, String> headers = (Map<String, String>) clazz.getMethod("getHeaders").invoke(attributes);
								delayExecution(headers);
							} else {
								callback.success(result);
							}
						} catch (ReflectiveOperationException | SecurityException e) {
							logger.info("No attributes found, handling as success");
							callback.error(createModuleException(RateLimiterError.UNEXPECTED_ATTRIBUTES_TYPE));
						}
					} else {
						callback.success(result);
					}
				}, (error, previous) -> {
					callback.error(error);
				});
			}
		}

		private void delayExecution(Map<String, String> headers) {
			retryIndex++;
			if (retryIndex <= numberOfRetries) {
				long delay = computeDelay(headers);
				logger.info("computed delay: {} ms", delay);
				waitTimes.storeWaitTime(id, delay + System.currentTimeMillis());
				scheduledExecutor.schedule(this, delay, TimeUnit.MILLISECONDS);
			} else {
				callback.error(createModuleException(RateLimiterError.RETRIES_EXHAUSTED));
			}
		}
		
		private long computeAdditionalJoinDelay() {
			BindingContext context = BindingContext.builder().build();
			TypedValue<?> expressionResult = expressionManager.evaluate(joinWaitTimeExpression, context);
			return extractLongResult(expressionResult);
		}

		private long computeDelay(Map<String, String> headers) {
			BindingContext context = BindingContext.builder()
					.addBinding("headers", TypedValue.of(headers))
					.addBinding("retryIndex", TypedValue.of(retryIndex)).build();
			TypedValue<?> expressionResult = expressionManager.evaluate(waitTimeExpression, context);
			return extractLongResult(expressionResult);
		}

		@SuppressWarnings("unchecked")
		private long extractLongResult(TypedValue<?> expressionResult) {
			long delay;
			DataType dataType = expressionResult.getDataType();
			if (Number.class.isAssignableFrom(dataType.getType())) {
				delay = ((TypedValue<Number>)expressionResult).getValue().longValue();
			} else if (String.class.isAssignableFrom(dataType.getType())) {
				String delayStr = ((TypedValue<String>)expressionResult).getValue();
				try {
					delay = Long.valueOf(delayStr);
				} catch (NumberFormatException e) {
					throw createModuleException(RateLimiterError.INVALID_NUMBER);
				}
			} else {
				throw createModuleException(RateLimiterError.INVALID_NUMBER);
			}
			return delay;
		}
		
		private ModuleException createModuleException(RateLimiterError e) {
			return new ModuleException(I18nMessageFactory.createStaticMessage(e.toString()), e);
		}
	}
}
