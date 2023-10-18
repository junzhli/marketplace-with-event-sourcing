/* (C)2022 */
package com.jeremyli.order.exception;

import java.text.MessageFormat;
import java.util.NoSuchElementException;

public class NoOrderFoundError extends NoSuchElementException {
    public NoOrderFoundError(String orderId) {
        super(MessageFormat.format("Order {0} not found", orderId));
    }
}
