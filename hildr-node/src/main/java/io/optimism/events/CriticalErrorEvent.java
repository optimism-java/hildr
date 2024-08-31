package io.optimism.events;

/**
 * The type CriticalErrorEvent.
 *
 * @author grapebaba
 * @since v0.4.1
 */
public class CriticalErrorEvent implements Event {

    private final Throwable cause;

    /**
     * Instantiates a new Reset event.
     *
     * @param cause the cause
     */
    public CriticalErrorEvent(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public EventType getType() {
        return EventType.CriticalErrorEvent;
    }

    @Override
    public String toString() {
        return "critical-error";
    }
}
