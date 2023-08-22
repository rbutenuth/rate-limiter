package de.codecentric.limiter.api;

import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.util.ArrayDeque;

public class BoundedBuffer implements BufferOps {

    @Parameter
    private int bufferSize;

    @Override
    public void offer(ArrayDeque<Runnable> queue, Runnable command) {
        if(queue.size() < bufferSize) {
            queue.offer(command);
        } else {
            throw new ModuleException(RateLimiterError.OVERFLOW, new IllegalStateException("Maximum buffer size exceeded"));
        }
    }

}
