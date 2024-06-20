package io.optimism.events;


import com.google.common.eventbus.Subscribe;

/**
 * The type NoopDeriver.
 *
 * @author grapebaba
 * @since v0.4.1
 */
public class NoopDeriver implements Deriver {
    @Override
    @Subscribe
    public void onEvent(Event event) {
        // do nothing
    }
}
