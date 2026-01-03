package com.flyroamy.mock.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Maya API Plan data structure
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanData {

    @JsonProperty("id")
    private String id;

    @JsonProperty("countries_enabled")
    private List<String> countriesEnabled;

    @JsonProperty("data_quota_bytes")
    private Long dataQuotaBytes;

    @JsonProperty("data_bytes_remaining")
    private Long dataBytesRemaining;

    @JsonProperty("start_time")
    private String startTime;

    @JsonProperty("end_time")
    private String endTime;

    @JsonProperty("network_status")
    private String networkStatus;

    @JsonProperty("product")
    private ProductData product;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getCountriesEnabled() {
        return countriesEnabled;
    }

    public void setCountriesEnabled(List<String> countriesEnabled) {
        this.countriesEnabled = countriesEnabled;
    }

    public Long getDataQuotaBytes() {
        return dataQuotaBytes;
    }

    public void setDataQuotaBytes(Long dataQuotaBytes) {
        this.dataQuotaBytes = dataQuotaBytes;
    }

    public Long getDataBytesRemaining() {
        return dataBytesRemaining;
    }

    public void setDataBytesRemaining(Long dataBytesRemaining) {
        this.dataBytesRemaining = dataBytesRemaining;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getNetworkStatus() {
        return networkStatus;
    }

    public void setNetworkStatus(String networkStatus) {
        this.networkStatus = networkStatus;
    }

    public ProductData getProduct() {
        return product;
    }

    public void setProduct(ProductData product) {
        this.product = product;
    }
}
