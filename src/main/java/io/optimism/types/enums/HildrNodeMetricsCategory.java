package io.optimism.types.enums;

import java.util.Optional;
import org.hyperledger.besu.plugin.services.metrics.MetricCategory;

/**
 * The enum HildrNodeMetricsCategory.
 *
 * @author grapebaba
 * @since 0.1.1
 */
public enum HildrNodeMetricsCategory implements MetricCategory {

    /** p2p network hildr node metrics category. */
    P2P_NETWORK("p2p_network");

    private final String name;

    HildrNodeMetricsCategory(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<String> getApplicationPrefix() {
        return Optional.of("hildr_node");
    }
}
