package io.optimism.batcher.compressor;

import io.optimism.batcher.config.Config;

/**
 * Compressor Config.
 *
 * @param targetFrameSize To target when creating channel frames. Note that if the realized
 *     compression ratio is worse than the approximate, more frames may actually be created. This
 *     also depends on how close the target is to the max frame size.
 * @param targetNumFrames To create in this channel. If the realized compression ratio is worse than
 *     approxComprRatio, additional leftover frame(s) might get created.
 * @param approxComprRatio ApproxComprRatio to assume. Should be slightly smaller than average from
 *     experiments to avoid the chances of creating a small additional leftover frame.
 * @param kind Kind of compressor to use. Must be one of KindKeys. If unset, NewCompressor will
 *     default to RatioKind.
 * @author thinkAfCod
 * @since 0.1.1
 */
public record CompressorConfig(int targetFrameSize, int targetNumFrames, String approxComprRatio, String kind) {

    /**
     * Create CompressorConfig instance from Config instance.
     *
     * @param config Config instance
     * @return CompressorConfig instance
     */
    public static CompressorConfig from(Config config) {
        return new CompressorConfig(
                config.targetFrameSize(), config.targetNumFrames(), config.approxComprRatio(), Compressors.RatioKind);
    }
}
