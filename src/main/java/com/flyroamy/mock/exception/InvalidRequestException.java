package com.flyroamy.mock.exception;

import java.util.Map;

public class InvalidRequestException extends RuntimeException {
    private final Map<String, Object> details;

    public InvalidRequestException(String message) {
        super(message);
        this.details = Map.of();
    }

    public InvalidRequestException(String message, Map<String, Object> details) {
        super(message);
        this.details = details;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
