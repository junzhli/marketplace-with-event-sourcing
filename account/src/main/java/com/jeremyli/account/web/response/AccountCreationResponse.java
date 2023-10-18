/* (C)2022 */
package com.jeremyli.account.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class AccountCreationResponse {
    public AccountCreationResponse(String accountId) {
        this.accountId = accountId;
    }

    @JsonProperty("id")
    private String accountId;
}
