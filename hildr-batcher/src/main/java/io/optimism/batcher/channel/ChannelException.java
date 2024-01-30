package io.optimism.batcher.channel;

/**
 * Batcher Channel Exception class.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class ChannelException extends RuntimeException {

    /**
     * Instantiates a new ChannelException.
     *
     * @param message the message
     */
    public ChannelException(String message) {
        super(message);
    }

    /**
     * Instantiates a new ChannelException.
     *
     * @param message the message
     * @param cause the cause
     */
    public ChannelException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new ChannelException.
     *
     * @param cause the cause
     */
    public ChannelException(Throwable cause) {
        super(cause);
    }
}
