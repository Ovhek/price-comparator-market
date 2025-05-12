package com.alexcruceat.pricecomparatormarket.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception to indicate that a requested resource was not found.
 * Annotated with {@link ResponseStatus} to automatically return an HTTP 404 Not Found status.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     *
     * @param message the detail message.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
