package com.logistics.bootstrap.web;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.logistics.shared.api.ErrorResponse;
import com.logistics.shared.exception.NotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle custom "Not Found" errors
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        ErrorResponse response = new ErrorResponse(
                false,
                new ErrorResponse.ErrorDetail(ex.getCode(), ex.getMessage(), null),
                UUID.randomUUID().toString(),
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // Handle DTO Validation errors (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<ErrorResponse.FieldError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage(), fe.getRejectedValue()))
                .toList();

        ErrorResponse response = new ErrorResponse(
                false,
                new ErrorResponse.ErrorDetail("VALIDATION_ERROR", "Validation failed", errors),
                UUID.randomUUID().toString(),
                Instant.now()
        );
        return ResponseEntity.badRequest().body(response);
    }

    // Catch-all for unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        ex.printStackTrace(); // In prod, log this properly
        ErrorResponse response = new ErrorResponse(
                false,
                new ErrorResponse.ErrorDetail("INTERNAL_ERROR", "An unexpected error occurred", null),
                UUID.randomUUID().toString(),
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}