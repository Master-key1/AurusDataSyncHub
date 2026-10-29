package com.auruspay.chase;

/**
 * Thrown when an ISO 8583 message fails structural or field-level validation
 * (e.g. an out-of-range length indicator, an unrecognized sub-tag length, or
 * a message that is shorter than its own bitmap claims).
 */
public class Iso8583ParseException extends Exception {

    private static final long serialVersionUID = 1L;

    public Iso8583ParseException(String message) {
        super(message);
    }

    public Iso8583ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
