package com.flyroamy.mock.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class SimulateUsageRequest {

    @NotBlank(message = "eSIM ID is required")
    private String esimId;

    @Min(value = 1, message = "Usage must be at least 1 MB")
    private int usageMB;

    private String timestamp;

    // Getters and Setters
    public String getEsimId() { return esimId; }
    public void setEsimId(String esimId) { this.esimId = esimId; }

    public int getUsageMB() { return usageMB; }
    public void setUsageMB(int usageMB) { this.usageMB = usageMB; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
