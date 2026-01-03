package com.flyroamy.mock.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Maya API Product data structure
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductData {

    @JsonProperty("uid")
    private String uid;

    @JsonProperty("name")
    private String name;

    @JsonProperty("countries_enabled")
    private List<String> countriesEnabled;

    @JsonProperty("data_quota_mb")
    private Integer dataQuotaMb;

    @JsonProperty("data_quota_bytes")
    private Long dataQuotaBytes;

    @JsonProperty("validity_days")
    private Integer validityDays;

    @JsonProperty("policy_id")
    private String policyId;

    @JsonProperty("policy_name")
    private String policyName;

    @JsonProperty("wholesale_price_usd")
    private Double wholesalePriceUsd;

    @JsonProperty("rrp_usd")
    private Double rrpUsd;

    @JsonProperty("rrp_eur")
    private Double rrpEur;

    @JsonProperty("rrp_gbp")
    private Double rrpGbp;

    @JsonProperty("rrp_cad")
    private Double rrpCad;

    @JsonProperty("rrp_aud")
    private Double rrpAud;

    @JsonProperty("rrp_jpy")
    private Double rrpJpy;

    @JsonProperty("unlimited_type")
    private String unlimitedType;

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getCountriesEnabled() {
        return countriesEnabled;
    }

    public void setCountriesEnabled(List<String> countriesEnabled) {
        this.countriesEnabled = countriesEnabled;
    }

    public Integer getDataQuotaMb() {
        return dataQuotaMb;
    }

    public void setDataQuotaMb(Integer dataQuotaMb) {
        this.dataQuotaMb = dataQuotaMb;
    }

    public Long getDataQuotaBytes() {
        return dataQuotaBytes;
    }

    public void setDataQuotaBytes(Long dataQuotaBytes) {
        this.dataQuotaBytes = dataQuotaBytes;
    }

    public Integer getValidityDays() {
        return validityDays;
    }

    public void setValidityDays(Integer validityDays) {
        this.validityDays = validityDays;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public Double getWholesalePriceUsd() {
        return wholesalePriceUsd;
    }

    public void setWholesalePriceUsd(Double wholesalePriceUsd) {
        this.wholesalePriceUsd = wholesalePriceUsd;
    }

    public Double getRrpUsd() {
        return rrpUsd;
    }

    public void setRrpUsd(Double rrpUsd) {
        this.rrpUsd = rrpUsd;
    }

    public Double getRrpEur() {
        return rrpEur;
    }

    public void setRrpEur(Double rrpEur) {
        this.rrpEur = rrpEur;
    }

    public Double getRrpGbp() {
        return rrpGbp;
    }

    public void setRrpGbp(Double rrpGbp) {
        this.rrpGbp = rrpGbp;
    }

    public Double getRrpCad() {
        return rrpCad;
    }

    public void setRrpCad(Double rrpCad) {
        this.rrpCad = rrpCad;
    }

    public Double getRrpAud() {
        return rrpAud;
    }

    public void setRrpAud(Double rrpAud) {
        this.rrpAud = rrpAud;
    }

    public Double getRrpJpy() {
        return rrpJpy;
    }

    public void setRrpJpy(Double rrpJpy) {
        this.rrpJpy = rrpJpy;
    }

    public String getUnlimitedType() {
        return unlimitedType;
    }

    public void setUnlimitedType(String unlimitedType) {
        this.unlimitedType = unlimitedType;
    }
}
