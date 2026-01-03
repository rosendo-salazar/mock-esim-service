package com.flyroamy.mock.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Standard Maya Mobile Connect+ API Response Wrapper
 * All API responses must use this wrapper format
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MayaApiResponse<T> {

    @JsonProperty("result")
    private Integer result;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("message")
    private String message;

    @JsonProperty("developer_message")
    private String developerMessage;

    @JsonProperty("esim")
    private EsimData esim;

    @JsonProperty("plan")
    private PlanData plan;

    @JsonProperty("iccid")
    private String iccid;

    @JsonProperty("products")
    private List<ProductData> products;

    @JsonProperty("product")
    private ProductData product;

    @JsonProperty("plans")
    private List<PlanData> plans;

    @JsonProperty("regions")
    private List<String> regions;

    @JsonProperty("balance")
    private BalanceData balance;

    // Additional fields can be added dynamically
    @JsonProperty("additional_data")
    private Map<String, Object> additionalData;

    public MayaApiResponse() {
        this.requestId = "req_" + UUID.randomUUID().toString().substring(0, 12);
    }

    public static <T> MayaApiResponse<T> success(Integer status) {
        MayaApiResponse<T> response = new MayaApiResponse<>();
        response.result = 1;
        response.status = status;
        response.message = "Success";
        return response;
    }

    public static <T> MayaApiResponse<T> error(Integer status, String message, String developerMessage) {
        MayaApiResponse<T> response = new MayaApiResponse<>();
        response.result = 0;
        response.status = status;
        response.message = message;
        response.developerMessage = developerMessage;
        return response;
    }

    // Getters and Setters
    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public void setDeveloperMessage(String developerMessage) {
        this.developerMessage = developerMessage;
    }

    public EsimData getEsim() {
        return esim;
    }

    public void setEsim(EsimData esim) {
        this.esim = esim;
    }

    public PlanData getPlan() {
        return plan;
    }

    public void setPlan(PlanData plan) {
        this.plan = plan;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public List<ProductData> getProducts() {
        return products;
    }

    public void setProducts(List<ProductData> products) {
        this.products = products;
    }

    public ProductData getProduct() {
        return product;
    }

    public void setProduct(ProductData product) {
        this.product = product;
    }

    public List<PlanData> getPlans() {
        return plans;
    }

    public void setPlans(List<PlanData> plans) {
        this.plans = plans;
    }

    public List<String> getRegions() {
        return regions;
    }

    public void setRegions(List<String> regions) {
        this.regions = regions;
    }

    public BalanceData getBalance() {
        return balance;
    }

    public void setBalance(BalanceData balance) {
        this.balance = balance;
    }

    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }
}
