package io.optimism.events;

import com.google.common.eventbus.Subscribe;
import java.util.function.Function;

/**
 * The type DeriverFunction.
 *
 * @author grapebaba
 * @since v0.4.1
 */
public class DeriverFunction implements Deriver {

    private final Function<Event, Void> function;

    /**
     * Instantiates a new Deriver function.
     *
     * @param function the function
     */
    public DeriverFunction(Function<Event, Void> function) {
        this.function = function;
    }

    @Override
    @Subscribe
    public void onEvent(Event event) {
        function.apply(event);
    }
}
