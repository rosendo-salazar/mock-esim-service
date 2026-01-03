package com.flyroamy.mock.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating an eSIM in Maya API
 */
public class CreateEsimRequest {

    @JsonProperty("plan_type_id")
    private String planTypeId;

    @JsonProperty("region")
    private String region;

    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("tag")
    private String tag;

    // Getters and Setters
    public String getPlanTypeId() {
        return planTypeId;
    }

    public void setPlanTypeId(String planTypeId) {
        this.planTypeId = planTypeId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
