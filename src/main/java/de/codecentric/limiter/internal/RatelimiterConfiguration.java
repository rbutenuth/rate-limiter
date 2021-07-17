package de.codecentric.limiter.internal;

import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.param.Parameter;

/**
 * This class represents an extension configuration, values set in this class are commonly used across multiple
 * operations since they represent something core from the extension.
 */
@Configuration(name="rate-limiter")
@Operations(RatelimiterOperations.class)
public class RatelimiterConfiguration {

	@Parameter
	private long minTimeBetweenOperations;

	@Parameter
	private TimeUnit unit;

	private static final long NEVER = -1;

	private LinkedList<Runnable> commandList;
	private long lastRun;

	public RatelimiterConfiguration() {
		lastRun = NEVER;
		commandList = new LinkedList<>();
	}

	public void schedule(ScheduledExecutorService scheduledExecutor, Runnable command) {
		long minTimeBetweenOperationsInMillis = unit.toMillis(minTimeBetweenOperations);
		synchronized (commandList) {
			boolean listWasEmpty = commandList.isEmpty();
			commandList.add(command);
			if (listWasEmpty) {
				long now = System.currentTimeMillis();
				if (lastRun == NEVER || now >= lastRun + minTimeBetweenOperationsInMillis) {
					scheduledExecutor.schedule(new Runner(scheduledExecutor), 0, TimeUnit.MILLISECONDS);
				} else {
					scheduledExecutor.schedule(new Runner(scheduledExecutor), lastRun + minTimeBetweenOperationsInMillis - now, TimeUnit.MILLISECONDS);
				}
			}
		}
	}
	
	private class Runner implements Runnable {
		ScheduledExecutorService scheduledExecutor;
		
		public Runner(ScheduledExecutorService scheduledExecutor) {
			this.scheduledExecutor = scheduledExecutor;
		}

		@Override
		public void run() {
			lastRun = System.currentTimeMillis();
			long minTimeBetweenOperationsInMillis = unit.toMillis(minTimeBetweenOperations);
			Runnable command;
			synchronized (commandList) {
				command = commandList.peek();
			}
			try {
				command.run();
			} finally {
				synchronized (commandList) {
					commandList.remove();
					if (!commandList.isEmpty()) {
						scheduledExecutor.schedule(this, minTimeBetweenOperationsInMillis, TimeUnit.MILLISECONDS);
					}
				}
			}
		}
	}
}
