package de.codecentric.limiter.api;

import java.util.ArrayDeque;
import java.util.Optional;

public class CommandQueue {

    private ArrayDeque<Runnable> commandQueue;
    private BufferOps bufferOps;

    public CommandQueue(BufferOps bufferOps) {
        this.bufferOps = bufferOps;
        this.commandQueue = new ArrayDeque<>();
    }

    synchronized public void push(Runnable command) {
        bufferOps.offer(this.commandQueue, command);
    }

    synchronized public boolean isEmpty() {
        return bufferOps.isEmpty(this.commandQueue);
    }

    synchronized public Optional<Runnable> pop() {
        return bufferOps.pop(this.commandQueue);
    }

}
