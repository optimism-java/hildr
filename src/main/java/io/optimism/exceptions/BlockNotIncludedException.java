package io.optimism.exceptions;

/**
 * The type BlockNotIncludedException.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public class BlockNotIncludedException extends RuntimeException {

    /** Instantiates a new Block not included exception. */
    public BlockNotIncludedException() {
        super("block not included");
    }

    /**
     * Instantiates a new Block not included exception.
     *
     * @param message the message
     */
    public BlockNotIncludedException(String message) {
        super(message);
    }
}
