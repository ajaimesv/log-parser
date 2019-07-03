package com.ef.exceptions;

/**
 * The type of exception thrown during the configuration phase.
 * @author Andres Jaimes
 */
public class ConfigurationException extends Exception {

    public ConfigurationException() {
        super();
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }

}
