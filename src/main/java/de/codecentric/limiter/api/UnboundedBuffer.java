package de.codecentric.limiter.api;

import java.util.ArrayDeque;

public class UnboundedBuffer implements BufferOps {

    @Override
    public void offer(ArrayDeque<Runnable> queue, Runnable command) {
        queue.offer(command);
    }

}
