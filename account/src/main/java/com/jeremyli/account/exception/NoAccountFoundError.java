/* (C)2022 */
package com.jeremyli.account.exception;

import java.text.MessageFormat;
import java.util.NoSuchElementException;

public class NoAccountFoundError extends NoSuchElementException {
    public NoAccountFoundError(String userId) {
        super(MessageFormat.format("UserId {0} not found", userId));
    }
}
