package com.flyroamy.mock.dto.response;

import java.util.Map;

public record ErrorResponse(
    String code,
    String message,
    Map<String, Object> details,
    String timestamp,
    String requestId
) {
    public Map<String, Object> toMap() {
        return Map.of("error", Map.of(
            "code", code,
            "message", message,
            "details", details,
            "timestamp", timestamp,
            "requestId", requestId
        ));
    }
}
