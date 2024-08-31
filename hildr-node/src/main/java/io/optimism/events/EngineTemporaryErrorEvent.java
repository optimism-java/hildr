package io.optimism.events;

/**
 * The type EngineTemporaryErrorEvent.
 *
 * @author grapebaba
 * @since v0.4.1
 */
public class EngineTemporaryErrorEvent implements Event {

    private final Throwable cause;

    /**
     * Instantiates a new Reset event.
     *
     * @param cause the cause
     */
    public EngineTemporaryErrorEvent(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public EventType getType() {
        return EventType.EngineTemporaryErrorEvent;
    }

    @Override
    public String toString() {
        return "engine-temporary-error";
    }
}
