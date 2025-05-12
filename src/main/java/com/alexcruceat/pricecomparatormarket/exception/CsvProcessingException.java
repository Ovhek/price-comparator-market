package com.alexcruceat.pricecomparatormarket.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception to indicate an error occurred during CSV file processing.
 * This could be due to parsing errors, data validation failures within the CSV,
 * or I/O issues related to CSV files.
 * Annotated with {@link ResponseStatus} to generally return an HTTP 500 Internal Server Error,
 * though specific handlers might choose a different status if the error is client-correctable.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // Or BAD_REQUEST if it's often due to bad input files
public class CsvProcessingException extends RuntimeException {
    /**
     * Constructs a new CsvProcessingException with the specified detail message.
     * @param message the detail message.
     */
    public CsvProcessingException(String message) {
        super(message);
    }
}