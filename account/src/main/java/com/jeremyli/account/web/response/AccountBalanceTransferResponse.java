/* (C)2022 */
package com.jeremyli.account.web.response;

import lombok.Getter;

@Getter
public class AccountBalanceTransferResponse {

    AccountBalanceTransferResponse() {}
    ;

    public AccountBalanceTransferResponse(boolean success) {
        this.success = success;
    }

    private boolean success;
}
