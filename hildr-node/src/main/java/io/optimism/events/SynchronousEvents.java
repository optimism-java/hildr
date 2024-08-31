package io.optimism.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;

/**
 * The type SynchronousEvents.
 *
 * @author grapebaba
 * @since v0.4.1
 */
public class SynchronousEvents implements EventEmitter {

    protected static final int SANITY_EVENT_LIMIT = 1000;

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SynchronousEvents.class);

    private final ReentrantLock lock = new ReentrantLock();

    private final List<Event> events;

    private final Deriver root;

    private final CompletableFuture<?> ctx;

    /**
     * Instantiates a new Synchronous events.
     *
     * @param root the root
     * @param ctx  the ctx
     */
    public SynchronousEvents(Deriver root, CompletableFuture<?> ctx) {
        this.root = root;
        this.ctx = ctx;
        this.events = new ArrayList<>();
    }

    @Override
    public void emit(Event event) {
        lock.lock();
        try {
            if (this.ctx.isCompletedExceptionally()) {
                LOGGER.warn("Context is completed exceptionally, ignore emitting event: {}", event);
                return;
            }

            if (events.size() >= SANITY_EVENT_LIMIT) {
                LOGGER.error("Event queue is full, drop event: {}", event);
                return;
            }

            events.add(event);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Drain.
     *
     * @throws Throwable the throwable
     */
    public void drain() throws Throwable {
        while (true) {
            if (ctx.isCompletedExceptionally()) {
                ctx.get();
            }

            lock.lock();
            Event firstEvent;
            try {
                if (events.isEmpty()) {
                    return;
                }

                firstEvent = events.removeFirst();
            } finally {
                lock.unlock();
            }

            root.onEvent(firstEvent);
        }
    }
}
