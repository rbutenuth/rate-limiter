package de.codecentric.limiter.api;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface BufferOps {

    void offer(ArrayDeque<Runnable> queue, Runnable command);

    default boolean isEmpty(ArrayDeque<Runnable> queue) {
        return queue.isEmpty();
    }

    default Optional<Runnable> pop(ArrayDeque<Runnable> queue) {
        try {
            return Optional.of(queue.pop());
        } catch (NoSuchElementException ex) {
            return Optional.empty();
        }
    }

}
