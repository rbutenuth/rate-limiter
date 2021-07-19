package de.codecentric.limiter.internal;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.lifecycle.Stoppable;
import org.mule.runtime.api.scheduler.SchedulerConfig;
import org.mule.runtime.api.scheduler.SchedulerService;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class RatelimiterOperations implements Stoppable, Startable {
	private static Logger logger = LoggerFactory.getLogger(RatelimiterOperations.class);

	@Inject
	private SchedulerService schedulerService;

	private ScheduledExecutorService scheduledExecutor;

	/**
	 * Delay the flow if called too fast.
	 */
	public void limitRate(@Config RatelimiterConfiguration configuration, CompletionCallback<Void, Void> callback) {
		logger.debug("schedule command");
		configuration.schedule(scheduledExecutor, new Runnable() {

			@Override
			public void run() {
				logger.debug("execute command");
				callback.success(Result.<Void, Void>builder().build());
			}
		});
	}
	
	public void fixedDelay(long delay, TimeUnit unit, CompletionCallback<Void, Void> callback) {
		logger.debug("delay: " + delay + ", unit: " + unit);
		scheduledExecutor.schedule(new Runnable() {

			@Override
			public void run() {
				logger.debug("execute delayed command");
				callback.success(Result.<Void, Void>builder().build());
			}
		}, delay, unit);
	}


	@Override
	public void start() {
		SchedulerConfig config = SchedulerConfig.config()
				.withMaxConcurrentTasks(10)
				.withShutdownTimeout(1, TimeUnit.SECONDS)
				.withPrefix("rate-limit")
				.withName("operations");
		scheduledExecutor = schedulerService.customScheduler(config);
	}

	@Override
	public void stop() {
		scheduledExecutor.shutdown();
	}
}
