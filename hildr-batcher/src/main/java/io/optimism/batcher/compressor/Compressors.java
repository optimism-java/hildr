package io.optimism.batcher.compressor;

import io.optimism.batcher.exception.UnsupportedException;

/**
 * Compressor create tool.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class Compressors {

    /** Kind type of ratio. */
    public static final String RatioKind = "ratio";

    private Compressors() {}

    /**
     * Create Compressor by kind.
     *
     * @param config Config of compressor
     * @return a compressor
     */
    public static Compressor create(final CompressorConfig config) {
        String kind = config.kind();
        if (kind.equalsIgnoreCase(RatioKind)) {
            return new RatioCompressor(config);
        } else {
            throw new UnsupportedException(String.format("unsupported kind: %s", kind));
        }
    }
}
