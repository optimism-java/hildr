package io.optimism.events;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class SynchronousEventsTest {

    @Test
    void testSynchronousEvents() {
        final int[] count = {0};
        DeriverFunction deriver = new DeriverFunction(a -> {
            count[0]++;
            return null;
        });

        CompletableFuture<?> ctx = new CompletableFuture<>();
        SynchronousEvents synchronousEvents = new SynchronousEvents(deriver, ctx);
        assertDoesNotThrow(synchronousEvents::drain);

        synchronousEvents.emit(new SynchronousDeriversTest.TestEvent());
        assertEquals(0, count[0]);
        try {
            synchronousEvents.drain();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        assertEquals(1, count[0]);

        synchronousEvents.emit(new SynchronousDeriversTest.TestEvent());
        synchronousEvents.emit(new SynchronousDeriversTest.TestEvent());
        assertEquals(1, count[0]);
        try {
            synchronousEvents.drain();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        assertEquals(3, count[0]);

        ctx.cancel(true);
        synchronousEvents.emit(new SynchronousDeriversTest.TestEvent());
        assertThrows(CancellationException.class, synchronousEvents::drain);
        assertEquals(3, count[0]);
    }

    @Test
    void testSynchronousEventsSanityLimit() {
        final int[] count = {0};
        DeriverFunction deriver = new DeriverFunction(a -> {
            count[0]++;
            return null;
        });

        CompletableFuture<?> ctx = new CompletableFuture<>();
        SynchronousEvents synchronousEvents = new SynchronousEvents(deriver, ctx);
        for (int i = 0; i < SynchronousEvents.SANITY_EVENT_LIMIT + 1; i++) {
            synchronousEvents.emit(new SynchronousDeriversTest.TestEvent());
        }
        assertDoesNotThrow(synchronousEvents::drain);
        assertEquals(SynchronousEvents.SANITY_EVENT_LIMIT, count[0]);

        synchronousEvents.emit(new SynchronousDeriversTest.TestEvent());
        assertDoesNotThrow(synchronousEvents::drain);
        assertEquals(SynchronousEvents.SANITY_EVENT_LIMIT + 1, count[0]);
    }

    public static class CyclicEvent implements Event {

        private final int count;

        public CyclicEvent(int count) {
            this.count = count;
        }

        @Override
        public EventType getType() {
            return null;
        }

        public String toString() {
            return "cyclic-event";
        }
    }

    public static class AF implements Function<Event, Void> {
        private EventEmitter emitter;

        private boolean result;

        public AF(EventEmitter emitter, boolean result) {
            this.emitter = emitter;
            this.result = result;
        }

        @Override
        public Void apply(Event e) {
            System.out.printf("received event: %s%n", e);
            if (e instanceof CyclicEvent) {
                if (((CyclicEvent) e).count < 10) {
                    emitter.emit(new CyclicEvent(((CyclicEvent) e).count + 1));
                } else {
                    result = true;
                }
            }
            return null;
        }
    }

    @Test
    void testSynchronousEventsCyclic() {
        final boolean result = false;
        AF af = new AF(null, result);
        DeriverFunction deriver = new DeriverFunction(af);

        CompletableFuture<?> ctx = new CompletableFuture<>();
        SynchronousEvents synchronousEvents = new SynchronousEvents(deriver, ctx);
        af.emitter = synchronousEvents;
        synchronousEvents.emit(new CyclicEvent(0));
        assertDoesNotThrow(synchronousEvents::drain);
        assertTrue(af.result);
    }
}
