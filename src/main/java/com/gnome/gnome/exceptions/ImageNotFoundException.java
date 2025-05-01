package com.gnome.gnome.exceptions;

/**
 * Custom exception for notify, that image not founded
 */
public class ImageNotFoundException extends RuntimeException {
    public ImageNotFoundException(String message) {
        super(message);
    }
}
