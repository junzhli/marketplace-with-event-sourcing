/* (C)2022 */
package com.jeremyli.account.exception;

import java.text.MessageFormat;

public class InsufficientBalanceError extends RuntimeException {
    public InsufficientBalanceError(String userId) {
        super(MessageFormat.format("UserId {0} with insufficient amount", userId));
    }
}
