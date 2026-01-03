package com.flyroamy.mock.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "mock_products")
public class MockProduct {

    @Id
    private String id;

    @Field("product_id")
    @Indexed(unique = true)
    private String productId;

    @Field("uid")
    @Indexed(unique = true)
    private String uid; // Maya API UID

    @Field("name")
    private String name;

    @Field("description")
    private String description;

    @Field("countries_enabled")
    private List<String> countriesEnabled;

    @Field("data_quota_mb")
    private Integer dataQuotaMb;

    @Field("data_quota_bytes")
    private Long dataQuotaBytes;

    @Field("validity_days")
    private Integer validityDays;

    @Field("policy_id")
    private String policyId;

    @Field("policy_name")
    private String policyName;

    @Field("wholesale_price_usd")
    private Double wholesalePriceUsd;

    @Field("rrp_usd")
    private Double rrpUsd;

    @Field("rrp_eur")
    private Double rrpEur;

    @Field("rrp_gbp")
    private Double rrpGbp;

    @Field("rrp_cad")
    private Double rrpCad;

    @Field("rrp_aud")
    private Double rrpAud;

    @Field("rrp_jpy")
    private Double rrpJpy;

    // Legacy fields for backward compatibility
    @Field("data_gb")
    private Double dataGB;

    @Field("price")
    private Double price;

    @Field("currency")
    private String currency = "USD";

    @Field("prices")
    private Map<String, Double> prices;

    @Field("wholesale_cost")
    private Double wholesaleCost;

    @Field("package_type")
    @Indexed
    private String packageType; // country, region, global

    @Field("countries")
    private List<String> countries;

    @Field("region")
    private String region;

    @Field("coverage")
    private Coverage coverage;

    @Field("is_active")
    private boolean isActive = true;

    @Field("unlimited_type")
    private String unlimitedType;

    @Field("terms")
    private String terms;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    public static class Coverage {
        private List<Network> networks;

        public List<Network> getNetworks() { return networks; }
        public void setNetworks(List<Network> networks) { this.networks = networks; }
    }

    public static class Network {
        private String mcc;
        private String mnc;
        private String operator;
        private String technology;

        public String getMcc() { return mcc; }
        public void setMcc(String mcc) { this.mcc = mcc; }
        public String getMnc() { return mnc; }
        public void setMnc(String mnc) { this.mnc = mnc; }
        public String getOperator() { return operator; }
        public void setOperator(String operator) { this.operator = operator; }
        public String getTechnology() { return technology; }
        public void setTechnology(String technology) { this.technology = technology; }
    }

    // Constructors
    public MockProduct() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getCountriesEnabled() { return countriesEnabled; }
    public void setCountriesEnabled(List<String> countriesEnabled) { this.countriesEnabled = countriesEnabled; }

    public Integer getDataQuotaMb() { return dataQuotaMb; }
    public void setDataQuotaMb(Integer dataQuotaMb) { this.dataQuotaMb = dataQuotaMb; }

    public Long getDataQuotaBytes() { return dataQuotaBytes; }
    public void setDataQuotaBytes(Long dataQuotaBytes) { this.dataQuotaBytes = dataQuotaBytes; }

    public Integer getValidityDays() { return validityDays; }
    public void setValidityDays(Integer validityDays) { this.validityDays = validityDays; }

    public String getPolicyId() { return policyId; }
    public void setPolicyId(String policyId) { this.policyId = policyId; }

    public String getPolicyName() { return policyName; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }

    public Double getWholesalePriceUsd() { return wholesalePriceUsd; }
    public void setWholesalePriceUsd(Double wholesalePriceUsd) { this.wholesalePriceUsd = wholesalePriceUsd; }

    public Double getRrpUsd() { return rrpUsd; }
    public void setRrpUsd(Double rrpUsd) { this.rrpUsd = rrpUsd; }

    public Double getRrpEur() { return rrpEur; }
    public void setRrpEur(Double rrpEur) { this.rrpEur = rrpEur; }

    public Double getRrpGbp() { return rrpGbp; }
    public void setRrpGbp(Double rrpGbp) { this.rrpGbp = rrpGbp; }

    public Double getRrpCad() { return rrpCad; }
    public void setRrpCad(Double rrpCad) { this.rrpCad = rrpCad; }

    public Double getRrpAud() { return rrpAud; }
    public void setRrpAud(Double rrpAud) { this.rrpAud = rrpAud; }

    public Double getRrpJpy() { return rrpJpy; }
    public void setRrpJpy(Double rrpJpy) { this.rrpJpy = rrpJpy; }

    public Double getDataGB() { return dataGB; }
    public void setDataGB(Double dataGB) { this.dataGB = dataGB; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Map<String, Double> getPrices() { return prices; }
    public void setPrices(Map<String, Double> prices) { this.prices = prices; }

    public Double getWholesaleCost() { return wholesaleCost; }
    public void setWholesaleCost(Double wholesaleCost) { this.wholesaleCost = wholesaleCost; }

    public String getPackageType() { return packageType; }
    public void setPackageType(String packageType) { this.packageType = packageType; }

    public List<String> getCountries() { return countries; }
    public void setCountries(List<String> countries) { this.countries = countries; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public Coverage getCoverage() { return coverage; }
    public void setCoverage(Coverage coverage) { this.coverage = coverage; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getUnlimitedType() { return unlimitedType; }
    public void setUnlimitedType(String unlimitedType) { this.unlimitedType = unlimitedType; }

    public String getTerms() { return terms; }
    public void setTerms(String terms) { this.terms = terms; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
