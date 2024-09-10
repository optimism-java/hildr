package io.optimism.exceptions;

/**
 * The type ConfigLoadException.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public class ConfigLoadException extends RuntimeException {

    /**
     * Instantiates a new Config load exception.
     *
     * @param throwable the throwable
     */
    public ConfigLoadException(Throwable throwable) {
        super(throwable);
    }
}
