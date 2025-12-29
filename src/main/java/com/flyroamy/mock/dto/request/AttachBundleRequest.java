package com.flyroamy.mock.dto.request;

import jakarta.validation.constraints.NotBlank;

public class AttachBundleRequest {

    @NotBlank(message = "Bundle ID is required")
    private String bundleId;

    private String activationType = "immediate";

    private boolean stackData = true;

    // Getters and Setters
    public String getBundleId() { return bundleId; }
    public void setBundleId(String bundleId) { this.bundleId = bundleId; }

    public String getActivationType() { return activationType; }
    public void setActivationType(String activationType) { this.activationType = activationType; }

    public boolean isStackData() { return stackData; }
    public void setStackData(boolean stackData) { this.stackData = stackData; }
}
