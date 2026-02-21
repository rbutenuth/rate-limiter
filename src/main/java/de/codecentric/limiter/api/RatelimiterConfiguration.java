package de.codecentric.limiter.api;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.sdk.api.annotation.Configuration;
import org.mule.sdk.api.annotation.Operations;
import org.mule.sdk.api.annotation.param.Parameter;
import org.mule.sdk.api.annotation.param.display.DisplayName;

@Configuration(name="rate-limiter")
@Operations(RatelimiterOperations.class)
public class RatelimiterConfiguration implements Initialisable {

    @Parameter
    private long minTimeBetweenOperations;

    @Parameter
    private TimeUnit unit;

    @Parameter
    @DisplayName("Buffer type")
    private BufferOps bufferOps;

    private static final long NEVER = -1;

    private volatile CommandQueue queue;

    private final Object lock = new Object();

    private final AtomicLong lastRun = new AtomicLong(NEVER);

    @Override
    public void initialise() {
        queue = new CommandQueue(bufferOps);
    }

    public void schedule(ScheduledExecutorService scheduledExecutor, Runnable command) {
        long minTimeBetweenOperationsInMillis = unit.toMillis(minTimeBetweenOperations);
        synchronized (lock) {
            boolean queueWasEmpty = queue.isEmpty();
            queue.push(command);
            if (queueWasEmpty) {
                long now = System.currentTimeMillis();
                long lr = lastRun.get();
                if (lr == NEVER || now >= lr + minTimeBetweenOperationsInMillis) {
                    scheduledExecutor.schedule(new Runner(scheduledExecutor), 0, TimeUnit.MILLISECONDS);
                } else {
                    scheduledExecutor.schedule(new Runner(scheduledExecutor), lr + minTimeBetweenOperationsInMillis - now, TimeUnit.MILLISECONDS);
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
            lastRun.set(System.currentTimeMillis());
            long minTimeBetweenOperationsInMillis = unit.toMillis(minTimeBetweenOperations);
            Optional<Runnable> command = queue.pop();
            try {
                command.ifPresent(Runnable::run);
            } finally {
                synchronized (lock) {
                    if (!queue.isEmpty()) {
                        scheduledExecutor.schedule(this, minTimeBetweenOperationsInMillis, TimeUnit.MILLISECONDS);
                    }
                }
            }
        }
    }

}
