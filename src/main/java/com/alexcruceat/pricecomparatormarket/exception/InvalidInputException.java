package com.alexcruceat.pricecomparatormarket.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception to indicate that the input provided by a client is invalid or malformed.
 * Annotated with {@link ResponseStatus} to automatically return an HTTP 400 Bad Request status.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) {
        super(message);
    }
}
