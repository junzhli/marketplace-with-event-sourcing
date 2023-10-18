/* (C)2022 */
package com.jeremyli.account.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jeremyli.account.domain.Account;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountInfoResponse {
    public AccountInfoResponse(Account account) {
        this.accountId = account.getUserId();
        this.fullName = account.getFullName();
        this.balance = account.getBalance();
    }

    @JsonProperty("id")
    private String accountId;

    private String fullName;
    private BigDecimal balance;
}
