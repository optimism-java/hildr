package io.optimism.events;

/**
 * The interface Deriver.
 *
 * @author grapebaba
 * @since v0.4.1
 */
public interface Deriver {

    /**
     * On event.
     *
     * @param event the event
     */
    void onEvent(Event event);
}
