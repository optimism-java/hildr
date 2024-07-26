package io.optimism.events;

/**
 * The EventEmitter interface.
 *
 * @author grapebaba
 * @since v0.4.1
 */
public interface EventEmitter {

    /**
     * Emit.
     *
     * @param event the event
     */
    void emit(Event event);
}
