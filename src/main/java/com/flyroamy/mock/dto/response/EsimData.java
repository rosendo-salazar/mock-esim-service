package com.flyroamy.mock.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maya API eSIM data structure
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EsimData {

    @JsonProperty("uid")
    private String uid;

    @JsonProperty("iccid")
    private String iccid;

    @JsonProperty("activation_code")
    private String activationCode;

    @JsonProperty("manual_code")
    private String manualCode;

    @JsonProperty("smdp_address")
    private String smdpAddress;

    @JsonProperty("auto_apn")
    private Boolean autoApn;

    @JsonProperty("apn")
    private String apn;

    @JsonProperty("state")
    private String state; // provisioned, active, suspended, deactivated, expired

    @JsonProperty("service_status")
    private String serviceStatus; // active, suspended, expired

    @JsonProperty("network_status")
    private String networkStatus; // connected, disconnected, roaming

    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("tag")
    private String tag;

    @JsonProperty("date_assigned")
    private String dateAssigned;

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public String getManualCode() {
        return manualCode;
    }

    public void setManualCode(String manualCode) {
        this.manualCode = manualCode;
    }

    public String getSmdpAddress() {
        return smdpAddress;
    }

    public void setSmdpAddress(String smdpAddress) {
        this.smdpAddress = smdpAddress;
    }

    public Boolean getAutoApn() {
        return autoApn;
    }

    public void setAutoApn(Boolean autoApn) {
        this.autoApn = autoApn;
    }

    public String getApn() {
        return apn;
    }

    public void setApn(String apn) {
        this.apn = apn;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(String serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public String getNetworkStatus() {
        return networkStatus;
    }

    public void setNetworkStatus(String networkStatus) {
        this.networkStatus = networkStatus;
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

    public String getDateAssigned() {
        return dateAssigned;
    }

    public void setDateAssigned(String dateAssigned) {
        this.dateAssigned = dateAssigned;
    }
}
