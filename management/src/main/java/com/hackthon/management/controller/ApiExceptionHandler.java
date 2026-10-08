package com.hackthon.management.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    // =========================
    // VALIDATION ERROR
    // =========================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": " +
                                error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));

        return response(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    // =========================
    // INVALID JSON / REQUEST
    // =========================

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMalformedRequest(
            HttpMessageNotReadableException exception) {

        return response(
                HttpStatus.BAD_REQUEST,
                "Request body is missing or malformed"
        );
    }

    // =========================
    // ILLEGAL ARGUMENT
    // =========================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(
            IllegalArgumentException exception) {

        exception.printStackTrace();

        return response(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    // =========================
    // ILLEGAL STATE
    // =========================

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleStateConflict(
            IllegalStateException exception) {

        exception.printStackTrace();

        return response(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    // =========================
    // DATABASE CONSTRAINT ERROR
    // =========================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(
            DataIntegrityViolationException exception) {

        exception.printStackTrace();

        String message =
                getRootCauseMessage(exception);

        return response(
                HttpStatus.CONFLICT,
                message
        );
    }

    // =========================
    // RUNTIME EXCEPTION
    // =========================

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(
            RuntimeException exception) {

        /*
         * Print the complete exception
         * in IntelliJ console.
         */
        exception.printStackTrace();

        String message = exception.getMessage();

        /*
         * If the exception itself has no message,
         * find the deepest/root cause.
         */
        if (message == null || message.isBlank()) {
            message = getRootCauseMessage(exception);
        }

        String normalized =
                message.toLowerCase();

        // =========================
        // NOT FOUND
        // =========================

        if (normalized.contains("not found")) {

            return response(
                    HttpStatus.NOT_FOUND,
                    message
            );
        }

        // =========================
        // CONFLICT
        // =========================

        if (normalized.contains("already has a project")
                || normalized.contains("already exists")) {

            return response(
                    HttpStatus.CONFLICT,
                    message
            );
        }

        /*
         * IMPORTANT:
         *
         * Previously this returned:
         *
         * "An unexpected server error occurred"
         *
         * which was hiding the real error.
         *
         * Now we return the actual exception message.
         */
        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                message
        );
    }

    // =========================
    // ROOT CAUSE
    // =========================

    private String getRootCauseMessage(
            Throwable exception) {

        Throwable rootCause = exception;

        while (rootCause.getCause() != null) {

            rootCause = rootCause.getCause();
        }

        if (rootCause.getMessage() != null
                && !rootCause.getMessage().isBlank()) {

            return rootCause.getMessage();
        }

        return "Unknown server error";
    }

    // =========================
    // CREATE RESPONSE
    // =========================

    private ResponseEntity<ApiError> response(
            HttpStatus status,
            String message) {

        return ResponseEntity
                .status(status)
                .body(
                        new ApiError(
                                Instant.now(),
                                status.value(),
                                status.getReasonPhrase(),
                                message
                        )
                );
    }

    // =========================
    // API ERROR DTO
    // =========================

    public record ApiError(
            Instant timestamp,
            int status,
            String error,
            String message
    ) {
    }
}