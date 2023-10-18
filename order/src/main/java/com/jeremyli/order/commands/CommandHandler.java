/* (C)2022 */
package com.jeremyli.order.commands;

public interface CommandHandler {
    void handle(OrderCreateCommand command);
}
