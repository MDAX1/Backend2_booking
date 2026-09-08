package com.backend1.backend1.exception;

/**
 * Kastas när bokningstjänsten inte kan nå kundtjänsten
 * (t.ex. kundtjänsten är nere, timeout, eller svarar med serverfel)
 * medan den försöker verifiera att en kund finns.
 */
public class CustomerServiceUnavailableException extends RuntimeException {

    public CustomerServiceUnavailableException(String message) {
        super(message);
    }

    public CustomerServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}