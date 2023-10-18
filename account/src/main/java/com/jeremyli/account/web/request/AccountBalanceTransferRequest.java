/* (C)2022 */
package com.jeremyli.account.web.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jeremyli.account.web.constraint.CheckValidTransfer;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@CheckValidTransfer(message = "Invalid account balance transfer request")
@Getter
public class AccountBalanceTransferRequest {

    public AccountBalanceTransferRequest() {
    }

    public AccountBalanceTransferRequest(String userIdA, String userIdB, BigDecimal amount) {
        this.userIdA = userIdA;
        this.userIdB = userIdB;
        this.amount = amount;
    }

    @JsonProperty("from")
    @NotNull
    private String userIdA;

    @JsonProperty("to")
    @NotNull
    private String userIdB;

    @Min(0)
    @NotNull
    private BigDecimal amount;
}
