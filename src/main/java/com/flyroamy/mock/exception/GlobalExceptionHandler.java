package com.flyroamy.mock.exception;

import com.flyroamy.mock.dto.response.ErrorResponse;
import com.flyroamy.mock.dto.response.MayaApiResponse;
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
    public ResponseEntity<MayaApiResponse<Void>> handleEsimNotFound(EsimNotFoundException ex) {
        logger.warn("eSIM not found: {}", ex.getMessage());

        MayaApiResponse<Void> response = MayaApiResponse.error(404, "eSIM not found", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<MayaApiResponse<Void>> handleProductNotFound(ProductNotFoundException ex) {
        logger.warn("Product not found: {}", ex.getMessage());

        MayaApiResponse<Void> response = MayaApiResponse.error(404, "Product not found", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<MayaApiResponse<Void>> handleInvalidRequest(InvalidRequestException ex) {
        logger.warn("Invalid request: {}", ex.getMessage());

        MayaApiResponse<Void> response = MayaApiResponse.error(400, "Invalid request", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(EsimAlreadyActiveException.class)
    public ResponseEntity<MayaApiResponse<Void>> handleEsimAlreadyActive(EsimAlreadyActiveException ex) {
        logger.warn("eSIM already active: {}", ex.getMessage());

        MayaApiResponse<Void> response = MayaApiResponse.error(409, "eSIM already active", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(EsimExpiredException.class)
    public ResponseEntity<MayaApiResponse<Void>> handleEsimExpired(EsimExpiredException ex) {
        logger.warn("eSIM expired: {}", ex.getMessage());

        MayaApiResponse<Void> response = MayaApiResponse.error(410, "eSIM expired", ex.getMessage());

        return ResponseEntity.status(HttpStatus.GONE).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MayaApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        logger.warn("Validation failed: {}", errors);

        MayaApiResponse<Void> response = MayaApiResponse.error(400, "Validation failed", "Request validation failed: " + errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MayaApiResponse<Void>> handleGenericException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);

        MayaApiResponse<Void> response = MayaApiResponse.error(500, "Internal server error", "An unexpected error occurred");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private String generateRequestId() {
        return "req_" + UUID.randomUUID().toString().substring(0, 12);
    }
}
