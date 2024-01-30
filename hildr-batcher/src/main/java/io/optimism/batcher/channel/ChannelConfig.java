package io.optimism.batcher.channel;

import io.optimism.batcher.config.Config;
import io.optimism.batcher.telemetry.BatcherMetrics;

/**
 * ChannelConfig class.
 *
 * @param channelTimeout The maximum number of L1 blocks that the inclusion transactions of a
 *     channel's frames can span.
 * @param maxChannelDuration Timeout of max block number.If 0, duration checks are disabled.
 * @param maxFrameSize The maximum byte-size a frame can have.
 * @param seqWindowSize The maximum byte-size a frame can have.
 * @param subSafetyMargin The maximum byte-size a frame can have.
 * @param metrics Batcher metrics
 * @author thinkAfCod
 * @since 0.1.1
 */
public record ChannelConfig(
        long channelTimeout,
        long maxChannelDuration,
        int maxFrameSize,
        long seqWindowSize,
        long subSafetyMargin,
        BatcherMetrics metrics) {

    /**
     * Create a ChannelConfig instance from Config instance.
     *
     * @param config Config instance
     * @return ChannelConfig instance
     */
    public static ChannelConfig from(Config config) {
        return new ChannelConfig(30000, 0, 120_000, 3600, 10, config.metrics());
    }
}
