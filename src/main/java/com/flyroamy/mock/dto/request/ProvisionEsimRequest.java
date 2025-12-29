package com.flyroamy.mock.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public class ProvisionEsimRequest {

    @NotBlank(message = "Bundle ID is required")
    private String bundleId;

    @Email(message = "Valid email is required")
    private String userEmail;

    private String profileType;

    private Map<String, Object> metadata;

    // Getters and Setters
    public String getBundleId() { return bundleId; }
    public void setBundleId(String bundleId) { this.bundleId = bundleId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getProfileType() { return profileType; }
    public void setProfileType(String profileType) { this.profileType = profileType; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
