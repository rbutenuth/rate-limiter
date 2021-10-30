package de.codecentric.limiter.api;

import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;

public class BoundedBuffer implements BufferOps {

    private static Logger logger = LoggerFactory.getLogger(BoundedBuffer.class);

    @Parameter
    private int bufferSize;

    @Parameter
    OverflowStrategy overflowStrategy;

    @Override
    public void offer(ArrayDeque<Runnable> queue, Runnable command) {
        if(queue.size() < bufferSize) {
            queue.offer(command);
        } else {
            switch (overflowStrategy) {
                case DROP_BUFFER:
                    logger.debug("DROP_BUFFER");
                    queue.clear();
                    queue.offer(command);
                    break;
                case DROP_NEW:
                    logger.debug("DROP_NEW");
                    // do nothing
                    break;
                case DROP_YOUNGEST:
                    logger.debug("DROP_YOUNGEST");
                    queue.removeLast();
                    queue.offer(command);
                    break;
                case DROP_OLDEST:
                    logger.debug("DROP_OLDEST");
                    queue.removeFirst();
                    queue.offer(command);
                    break;
            }
        }
    }

    public enum OverflowStrategy {
        DROP_NEW, DROP_OLDEST, DROP_YOUNGEST, DROP_BUFFER
    }

}
