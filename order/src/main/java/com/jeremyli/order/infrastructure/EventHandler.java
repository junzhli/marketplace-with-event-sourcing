/* (C)2022 */
package com.jeremyli.order.infrastructure;

import com.jeremyli.common.events.AccountOrderDebitedEvent;
import com.jeremyli.common.events.AccountOrderRejectedEvent;
import com.jeremyli.common.events.OrderDebitedEvent;

public interface EventHandler {
    void handle(AccountOrderDebitedEvent event);

    void handle(AccountOrderRejectedEvent event);

    void handle(OrderDebitedEvent event);
}
