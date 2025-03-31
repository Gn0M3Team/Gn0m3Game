package com.gnome.gnome.exceptions;

/**
 * Custom exception for S3 actions.
 * This exception is thrown when an S3 operation fails.
 */
public class S3ActionException extends RuntimeException {

    /**
     * Constructs a new S3ActionException with the specified detail message.
     *
     * @param message the detail message.
     */
    public S3ActionException(String message) {
        super(message);
    }

    /**
     * Constructs a new S3ActionException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause of the exception.
     */
    public S3ActionException(String message, Throwable cause) {
        super(message, cause);
    }
}