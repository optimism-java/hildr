package io.optimism.batcher.channel;

/**
 * ChannelFullException class.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class ChannelFullException extends ChannelException {

    /**
     * Instantiates a new ChannelFullException.
     *
     * @param message the message
     */
    public ChannelFullException(String message) {
        super(message);
    }

    /**
     * Instantiates a new ChannelFullException.
     *
     * @param message the message
     * @param cause the cause
     */
    public ChannelFullException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new ChannelFullException.
     *
     * @param cause the cause
     */
    public ChannelFullException(Throwable cause) {
        super(cause);
    }
}
