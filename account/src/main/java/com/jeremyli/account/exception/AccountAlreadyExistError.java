/* (C)2022 */
package com.jeremyli.account.exception;

import java.text.MessageFormat;

public class AccountAlreadyExistError extends RuntimeException {
    AccountAlreadyExistError(String userId) {
        super(MessageFormat.format("UserId {0} already exist", userId));
    }
}
