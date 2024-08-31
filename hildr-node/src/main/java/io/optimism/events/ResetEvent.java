package io.optimism.events;

/**
 * The type ResetEvent.
 *
 * @author grapebaba
 * @since v0.4.1
 */
public class ResetEvent implements Event {

    private final Throwable cause;

    /**
     * Instantiates a new Reset event.
     *
     * @param cause the cause
     */
    public ResetEvent(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public EventType getType() {
        return EventType.ResetEvent;
    }

    @Override
    public String toString() {
        return "reset-event";
    }
}
