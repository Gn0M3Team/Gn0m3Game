package com.gnome.gnome.exceptions;

/**
 * Custom exception for data access related errors.
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}