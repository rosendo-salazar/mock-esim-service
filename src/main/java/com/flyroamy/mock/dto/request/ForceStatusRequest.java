package com.flyroamy.mock.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ForceStatusRequest {

    @NotBlank(message = "eSIM ID is required")
    private String esimId;

    @NotBlank(message = "New status is required")
    private String newStatus;

    private String reason;

    // Getters and Setters
    public String getEsimId() { return esimId; }
    public void setEsimId(String esimId) { this.esimId = esimId; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
