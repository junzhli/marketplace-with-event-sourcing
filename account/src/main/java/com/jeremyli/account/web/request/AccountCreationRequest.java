/* (C)2022 */
package com.jeremyli.account.web.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class AccountCreationRequest {
    public AccountCreationRequest() {}

    public AccountCreationRequest(String userId, String name, BigDecimal balance) {
        this.userId = userId;
        this.name = name;
        this.balance = balance;
    }

    @JsonProperty("id")
    private String userId;

    @NotNull(message = "name must not be null")
    private String name;

    @NotNull(message = "balance must not be null")
    @Min(value = 0, message = "balance must be > 0")
    private BigDecimal balance;

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
