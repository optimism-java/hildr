package io.optimism.exceptions;

/**
 * The type BlockIsPreLondonException.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public class BlockIsPreLondonException extends RuntimeException {

    /** Instantiates a new Block is pre london exception. */
    public BlockIsPreLondonException() {
        super("block is pre london");
    }
}
