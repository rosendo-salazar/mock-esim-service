package com.flyroamy.mock.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "mock_bundles")
public class MockBundle {

    @Id
    private String id;

    @Field("bundle_id")
    @Indexed(unique = true)
    private String bundleId;

    @Field("product_id")
    private Integer productId;

    @Field("name")
    private String name;

    @Field("description")
    private String description;

    @Field("data_gb")
    private Double dataGB;

    @Field("validity_days")
    private Integer validityDays;

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

    @Field("badge")
    private String badge;

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
    public MockBundle() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBundleId() { return bundleId; }
    public void setBundleId(String bundleId) { this.bundleId = bundleId; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getDataGB() { return dataGB; }
    public void setDataGB(Double dataGB) { this.dataGB = dataGB; }

    public Integer getValidityDays() { return validityDays; }
    public void setValidityDays(Integer validityDays) { this.validityDays = validityDays; }

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

    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }

    public String getUnlimitedType() { return unlimitedType; }
    public void setUnlimitedType(String unlimitedType) { this.unlimitedType = unlimitedType; }

    public String getTerms() { return terms; }
    public void setTerms(String terms) { this.terms = terms; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
