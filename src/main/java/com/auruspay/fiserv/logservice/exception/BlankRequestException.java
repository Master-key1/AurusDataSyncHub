package com.auruspay.logservice.exception;

/**
 * Thrown when a required {@code cctRequest} field is missing or blank.
 * Lets the decrypt step signal "bad input" separately from "decryption threw".
 */
public class BlankRequestException extends RuntimeException {

    public BlankRequestException(String message) {
        super(message);
    }
}