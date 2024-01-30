package io.optimism.batcher.compressor.exception;

/**
 * If the compressor is full and no more data should be written or the compressor is known to be
 * full.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class CompressorException extends RuntimeException {

    /**
     * Constructor of CompressorFullException.
     *
     * @param message error message
     */
    public CompressorException(String message) {
        super(message);
    }
}
