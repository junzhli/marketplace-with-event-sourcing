/* (C)2022 */
package com.jeremyli.account.service;

import com.jeremyli.common.events.OrderCreatedEventV2;
import com.jeremyli.common.events.deprecated.OrderCreatedEventV1;

public interface EventHandler {
    void handle(OrderCreatedEventV2 event);

    void handle(OrderCreatedEventV1 event);
}
