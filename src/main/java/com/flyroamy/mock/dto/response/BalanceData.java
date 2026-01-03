package com.flyroamy.mock.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maya API Balance data structure
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BalanceData {

    @JsonProperty("balance")
    private Double balance;

    @JsonProperty("currency")
    private String currency;

    // Getters and Setters
    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
