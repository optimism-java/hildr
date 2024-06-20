package io.optimism.events;

import com.google.common.eventbus.Subscribe;

import java.util.List;


public class SynchronousDerivers implements Deriver {

    private final List<Deriver> derivers;

    public SynchronousDerivers(List<Deriver> derivers) {
        this.derivers = derivers;
    }

    @Override
    @Subscribe
    public void onEvent(Event event) {
        for (Deriver deriver : derivers) {
            deriver.onEvent(event);
        }
    }
}
