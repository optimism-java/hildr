package io.optimism.exceptions;

/**
 * The type DecompressException.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public class DecompressException extends RuntimeException {

    /**
     * Instantiates a new Decompress zlib exception.
     *
     * @param throwable the throwable
     */
    public DecompressException(Throwable throwable) {
        super(throwable);
    }
}
