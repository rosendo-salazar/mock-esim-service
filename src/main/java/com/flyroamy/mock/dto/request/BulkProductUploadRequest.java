package com.flyroamy.mock.dto.request;

import java.util.List;

/**
 * Request body for bulk uploading products.
 * Supports the same Excel format as the admin panel plans upload.
 */
public class BulkProductUploadRequest {

    private List<ProductUploadItem> products;

    public List<ProductUploadItem> getProducts() {
        return products;
    }

    public void setProducts(List<ProductUploadItem> products) {
        this.products = products;
    }

    /**
     * Individual product item in bulk upload.
     * Field names match the Excel format used by admin panel.
     */
    public static class ProductUploadItem {
        private String productId;
        private String name;
        private String description;
        private List<String> countriesEnabled;
        private Integer dataQuotaMb;
        private Long dataQuotaBytes;
        private Integer validityDays;
        private String policyId;
        private String policyName;
        private Double wholesalePriceUsd;
        private Double rrpUsd;
        private Double rrpEur;
        private Double rrpGbp;
        private Double rrpCad;
        private Double rrpAud;
        private Double rrpJpy;
        private String packageType;
        private String region;
        private Boolean isActive;
        private String unlimitedType;
        private String terms;

        // Excel format fields (alternative naming from spreadsheet)
        private Double data;  // data in GB (converted to MB)
        private Integer days;  // alias for validityDays
        private Double wholesaleCost;  // alias for wholesalePriceUsd
        private Double price;  // alias for rrpUsd
        private String provider;
        private String providerId;
        private String planDataDays;  // combined data/days string
        private String countries;  // comma-separated countries (converted to list)

        // Getters and Setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
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
        public String getPackageType() { return packageType; }
        public void setPackageType(String packageType) { this.packageType = packageType; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        public String getUnlimitedType() { return unlimitedType; }
        public void setUnlimitedType(String unlimitedType) { this.unlimitedType = unlimitedType; }
        public String getTerms() { return terms; }
        public void setTerms(String terms) { this.terms = terms; }

        // Excel format field getters/setters
        public Double getData() { return data; }
        public void setData(Double data) { this.data = data; }
        public Integer getDays() { return days; }
        public void setDays(Integer days) { this.days = days; }
        public Double getWholesaleCost() { return wholesaleCost; }
        public void setWholesaleCost(Double wholesaleCost) { this.wholesaleCost = wholesaleCost; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public String getProviderId() { return providerId; }
        public void setProviderId(String providerId) { this.providerId = providerId; }
        public String getPlanDataDays() { return planDataDays; }
        public void setPlanDataDays(String planDataDays) { this.planDataDays = planDataDays; }
        public String getCountries() { return countries; }
        public void setCountries(String countries) { this.countries = countries; }
    }
}
