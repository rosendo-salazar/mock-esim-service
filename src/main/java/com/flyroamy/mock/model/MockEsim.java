package com.flyroamy.mock.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "mock_esims")
public class MockEsim {

    @Id
    private String id;

    @Field("esim_id")
    @Indexed(unique = true)
    private String esimId;

    @Field("iccid")
    @Indexed(unique = true)
    private String iccid;

    @Field("matching_id")
    private String matchingId;

    @Field("qr_code_url")
    private String qrCodeUrl;

    @Field("qr_code_data")
    private String qrCodeData;

    @Field("activation_code")
    private String activationCode;

    @Field("status")
    @Indexed
    private String status; // provisioned, active, suspended, deactivated, expired

    @Field("user_email")
    private String userEmail;

    @Field("profile_type")
    private String profileType;

    @Field("metadata")
    private java.util.Map<String, Object> metadata;

    @Field("attached_bundles")
    private List<AttachedBundle> attachedBundles = new ArrayList<>();

    @Field("total_data_allowance_mb")
    private Integer totalDataAllowanceMB = 0;

    @Field("total_data_used_mb")
    private Integer totalDataUsedMB = 0;

    @Field("activation_date")
    private LocalDateTime activationDate;

    @Field("last_used")
    private LocalDateTime lastUsed;

    @Field("network")
    private NetworkInfo network;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    public static class AttachedBundle {
        private String bundleId;
        private String bundleName;
        private LocalDateTime attachedAt;
        private LocalDateTime expiryDate;
        private Integer dataAllowanceMB;
        private Integer dataUsedMB = 0;
        private Integer remainingDataMB;
        private String status; // active, expired, depleted
        private List<String> countries;
        private String packageType;

        // Getters and Setters
        public String getBundleId() { return bundleId; }
        public void setBundleId(String bundleId) { this.bundleId = bundleId; }

        public String getBundleName() { return bundleName; }
        public void setBundleName(String bundleName) { this.bundleName = bundleName; }

        public LocalDateTime getAttachedAt() { return attachedAt; }
        public void setAttachedAt(LocalDateTime attachedAt) { this.attachedAt = attachedAt; }

        public LocalDateTime getExpiryDate() { return expiryDate; }
        public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

        public Integer getDataAllowanceMB() { return dataAllowanceMB; }
        public void setDataAllowanceMB(Integer dataAllowanceMB) { this.dataAllowanceMB = dataAllowanceMB; }

        public Integer getDataUsedMB() { return dataUsedMB; }
        public void setDataUsedMB(Integer dataUsedMB) { this.dataUsedMB = dataUsedMB; }

        public Integer getRemainingDataMB() { return remainingDataMB; }
        public void setRemainingDataMB(Integer remainingDataMB) { this.remainingDataMB = remainingDataMB; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public List<String> getCountries() { return countries; }
        public void setCountries(List<String> countries) { this.countries = countries; }

        public String getPackageType() { return packageType; }
        public void setPackageType(String packageType) { this.packageType = packageType; }
    }

    public static class NetworkInfo {
        private String mcc;
        private String mnc;
        private String operator;

        public String getMcc() { return mcc; }
        public void setMcc(String mcc) { this.mcc = mcc; }
        public String getMnc() { return mnc; }
        public void setMnc(String mnc) { this.mnc = mnc; }
        public String getOperator() { return operator; }
        public void setOperator(String operator) { this.operator = operator; }
    }

    // Constructors
    public MockEsim() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.attachedBundles = new ArrayList<>();
    }

    // Helper methods
    public void addBundle(AttachedBundle bundle) {
        if (this.attachedBundles == null) {
            this.attachedBundles = new ArrayList<>();
        }
        this.attachedBundles.add(bundle);
        recalculateTotals();
    }

    public void recalculateTotals() {
        this.totalDataAllowanceMB = attachedBundles.stream()
            .filter(b -> "active".equals(b.getStatus()))
            .mapToInt(AttachedBundle::getDataAllowanceMB)
            .sum();
        this.totalDataUsedMB = attachedBundles.stream()
            .mapToInt(b -> b.getDataUsedMB() != null ? b.getDataUsedMB() : 0)
            .sum();
    }

    public Integer getTotalRemainingDataMB() {
        return attachedBundles.stream()
            .filter(b -> "active".equals(b.getStatus()))
            .mapToInt(b -> b.getRemainingDataMB() != null ? b.getRemainingDataMB() : 0)
            .sum();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEsimId() { return esimId; }
    public void setEsimId(String esimId) { this.esimId = esimId; }

    public String getIccid() { return iccid; }
    public void setIccid(String iccid) { this.iccid = iccid; }

    public String getMatchingId() { return matchingId; }
    public void setMatchingId(String matchingId) { this.matchingId = matchingId; }

    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }

    public String getQrCodeData() { return qrCodeData; }
    public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }

    public String getActivationCode() { return activationCode; }
    public void setActivationCode(String activationCode) { this.activationCode = activationCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getProfileType() { return profileType; }
    public void setProfileType(String profileType) { this.profileType = profileType; }

    public java.util.Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(java.util.Map<String, Object> metadata) { this.metadata = metadata; }

    public List<AttachedBundle> getAttachedBundles() { return attachedBundles; }
    public void setAttachedBundles(List<AttachedBundle> attachedBundles) { this.attachedBundles = attachedBundles; }

    public Integer getTotalDataAllowanceMB() { return totalDataAllowanceMB; }
    public void setTotalDataAllowanceMB(Integer totalDataAllowanceMB) { this.totalDataAllowanceMB = totalDataAllowanceMB; }

    public Integer getTotalDataUsedMB() { return totalDataUsedMB; }
    public void setTotalDataUsedMB(Integer totalDataUsedMB) { this.totalDataUsedMB = totalDataUsedMB; }

    public LocalDateTime getActivationDate() { return activationDate; }
    public void setActivationDate(LocalDateTime activationDate) { this.activationDate = activationDate; }

    public LocalDateTime getLastUsed() { return lastUsed; }
    public void setLastUsed(LocalDateTime lastUsed) { this.lastUsed = lastUsed; }

    public NetworkInfo getNetwork() { return network; }
    public void setNetwork(NetworkInfo network) { this.network = network; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
