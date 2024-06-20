package io.optimism.events;


/**
 * The type NoopEmitter.
 *
 * @author grapebaba
 * @since v0.4.1
 */
public class NoopEmitter implements EventEmitter {
    @Override
    public void emit(Event event) {
        // do nothing
    }
}
