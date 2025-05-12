package com.alexcruceat.pricecomparatormarket.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the REST APIs.
 * This class uses {@link ControllerAdvice} to intercept exceptions thrown by controllers
 * and provide a standardized JSON error response.
 * It extends {@link ResponseEntityExceptionHandler} to leverage its handling of standard Spring MVC exceptions.
 */
@ControllerAdvice
public class GlobalApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalApiExceptionHandler.class);

    /**
     * Handles {@link ResourceNotFoundException}.
     * Logs a warning and returns an HTTP 404 Not Found response.
     *
     * @param ex      The {@link ResourceNotFoundException} that was thrown.
     * @param request The current web request.
     * @return A {@link ResponseEntity} containing the error details and HTTP status 404.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    /**
     * Handles {@link InvalidInputException}.
     * Logs a warning and returns an HTTP 400 Bad Request response.
     *
     * @param ex      The {@link InvalidInputException} that was thrown.
     * @param request The current web request.
     * @return A {@link ResponseEntity} containing the error details and HTTP status 400.
     */
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<Object> handleInvalidInputException(
            InvalidInputException ex, WebRequest request) {
        log.warn("Invalid input: {}", ex.getMessage());
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handles {@link CsvProcessingException}.
     * Logs an error and returns an HTTP 500 Internal Server Error response.
     *
     * @param ex      The {@link CsvProcessingException} that was thrown.
     * @param request The current web request.
     * @return A {@link ResponseEntity} containing the error details and HTTP status 500.
     */
    @ExceptionHandler(CsvProcessingException.class)
    public ResponseEntity<Object> handleCsvProcessingException(
            CsvProcessingException ex, WebRequest request) {
        log.error("CSV Processing error: {}", ex.getMessage(), ex);
        return buildErrorResponse(ex, "Error processing CSV file.", HttpStatus.INTERNAL_SERVER_ERROR, request);
    }


    /**
     * Overrides the handling for {@link MethodArgumentNotValidException}, which is thrown
     * when validation on an argument annotated with {@code @Valid} fails.
     * Returns an HTTP 400 Bad Request with detailed validation error messages.
     *
     * @param ex      The {@link MethodArgumentNotValidException} instance.
     * @param headers The headers to be written to the response.
     * @param status  The status code to be_written to the response.
     * @param request The current request.
     * @return A {@link ResponseEntity} containing the structured error response.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("Validation error: {}", ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", "Validation Failed");

        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        body.put("message", errors);
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, headers, status);
    }

    /**
     * Handles all other unhandled exceptions as a fallback.
     * Logs an error and returns an HTTP 500 Internal Server Error response.
     *
     * @param ex      The generic {@link Exception} that was thrown.
     * @param request The current web request.
     * @return A {@link ResponseEntity} containing a generic error message and HTTP status 500.
     */
    @ExceptionHandler(Exception.class) // Catch-all for any other exceptions
    public ResponseEntity<Object> handleGenericException(
            Exception ex, WebRequest request) {
        log.error("An unexpected error occurred: ", ex);
        return buildErrorResponse(ex, "An unexpected internal server error occurred. Please contact support.", HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * Helper method to build a standardized error response body.
     *
     * @param ex      The exception that occurred.
     * @param status  The HTTP status for the response.
     * @param request The current web request.
     * @return A {@link ResponseEntity} containing the error details.
     */
    private ResponseEntity<Object> buildErrorResponse(
            Exception ex, HttpStatus status, WebRequest request) {
        return buildErrorResponse(ex, ex.getMessage(), status, request);
    }

    /**
     * Helper method to build a standardized error response body with a custom message.
     *
     * @param ex      The exception that occurred (can be used for logging or specific details).
     * @param message The custom error message to include in the response.
     * @param status  The HTTP status for the response.
     * @param request The current web request.
     * @return A {@link ResponseEntity} containing the error details.
     */
    private ResponseEntity<Object> buildErrorResponse(
            Exception ex, String message, HttpStatus status, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString()); // Ensure consistent ISO format
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, status);
    }
}