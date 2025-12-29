package com.flyroamy.mock.exception;

import com.flyroamy.mock.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EsimNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEsimNotFound(EsimNotFoundException ex) {
        logger.warn("eSIM not found: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
            "ESIM_NOT_FOUND",
            ex.getMessage(),
            Map.of("esimId", ex.getEsimId()),
            Instant.now().toString(),
            generateRequestId()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BundleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBundleNotFound(BundleNotFoundException ex) {
        logger.warn("Bundle not found: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
            "BUNDLE_NOT_FOUND",
            ex.getMessage(),
            Map.of("bundleId", ex.getBundleId()),
            Instant.now().toString(),
            generateRequestId()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRequestException ex) {
        logger.warn("Invalid request: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
            "INVALID_REQUEST",
            ex.getMessage(),
            ex.getDetails(),
            Instant.now().toString(),
            generateRequestId()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(EsimAlreadyActiveException.class)
    public ResponseEntity<ErrorResponse> handleEsimAlreadyActive(EsimAlreadyActiveException ex) {
        logger.warn("eSIM already active: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
            "ESIM_ALREADY_ACTIVE",
            ex.getMessage(),
            Map.of("esimId", ex.getEsimId()),
            Instant.now().toString(),
            generateRequestId()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(EsimExpiredException.class)
    public ResponseEntity<ErrorResponse> handleEsimExpired(EsimExpiredException ex) {
        logger.warn("eSIM expired: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
            "ESIM_EXPIRED",
            ex.getMessage(),
            Map.of("esimId", ex.getEsimId()),
            Instant.now().toString(),
            generateRequestId()
        );
        return ResponseEntity.status(HttpStatus.GONE).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        logger.warn("Validation failed: {}", errors);
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_FAILED",
            "Request validation failed",
            errors,
            Instant.now().toString(),
            generateRequestId()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_ERROR",
            "An unexpected error occurred",
            Map.of(),
            Instant.now().toString(),
            generateRequestId()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private String generateRequestId() {
        return "req_" + UUID.randomUUID().toString().substring(0, 12);
    }
}
