package io.optimism.events;

import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;

/**
 * The type DebugDeriver.
 *
 * @author grapebaba
 * @since v0.4.1
 */
public class DebugDeriver<E extends Event> implements Deriver {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DebugDeriver.class);

    @Override
    @Subscribe
    public void onEvent(Event event) {
        LOGGER.debug("Event: {}", event);
    }
}
