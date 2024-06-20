package io.optimism.events;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SynchronousDeriversTest {

    public static class TestEvent implements Event {

        @Override
        public EventType getType() {
            return null;
        }

        public String toString() {
            return "X";
        }
    }

    @Test
    void testSynchronousDeriversOnEvents() {
        final String[] res = {""};

        DeriverFunction a = new DeriverFunction(event -> {
            res[0] += String.format("A:%s\n", event);
            return null;
        });

        DeriverFunction b = new DeriverFunction(event -> {
            res[0] += String.format("B:%s\n", event);
            return null;
        });

        DeriverFunction c = new DeriverFunction(event -> {
            res[0] += String.format("c:%s\n", event);
            return null;
        });


        SynchronousDerivers x = new SynchronousDerivers(List.of());
        x.onEvent(new TestEvent());
        assertEquals("", res[0]);

        x = new SynchronousDerivers(List.of(a));
        x.onEvent(new TestEvent());
        assertEquals("A:X\n", res[0]);

        res[0] = "";
        x = new SynchronousDerivers(List.of(a, a));
        x.onEvent(new TestEvent());
        assertEquals("A:X\nA:X\n", res[0]);

        res[0] = "";
        x = new SynchronousDerivers(List.of(a, b));
        x.onEvent(new TestEvent());
        assertEquals("A:X\nB:X\n", res[0]);

        res[0] = "";
        x = new SynchronousDerivers(List.of(a, b, c));
        x.onEvent(new TestEvent());
        assertEquals("A:X\nB:X\nc:X\n", res[0]);
    }
}