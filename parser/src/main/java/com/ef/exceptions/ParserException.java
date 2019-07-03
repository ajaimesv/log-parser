package com.ef.exceptions;

/**
 * The type of exception thrown during the parsing phase.
 * @author Andres Jaimes
 */
public class ParserException extends Exception {

    public ParserException() {
        super();
    }

    public ParserException(String message) {
        super(message);
    }

    public ParserException(Throwable cause) {
        super(cause);
    }

}
