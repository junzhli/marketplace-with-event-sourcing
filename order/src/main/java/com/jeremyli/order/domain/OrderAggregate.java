/* (C)2022 */
package com.jeremyli.order.domain;

import com.jeremyli.common.events.OrderCreatedEventV2;
import com.jeremyli.common.events.ShippingMethod;
import com.jeremyli.common.events.deprecated.OrderCreatedEventV1;
import com.jeremyli.order.commands.OrderCreateCommand;
import com.jeremyli.order.domain.deprecated.OrderAggregateV1;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
@Getter
@ToString(callSuper = true)
public class OrderAggregate extends OrderAggregateV1 {
    private final int aggregateVersion = 2;

    private ShippingMethod shippingMethod;

    public OrderAggregate(OrderCreateCommand orderCreateCommand) {
        raiseEvent(
                OrderCreatedEventV2.builder()
                        .id(orderCreateCommand.getId())
                        .orderByAccountId(orderCreateCommand.getOrderByAccountId())
                        .amount(orderCreateCommand.getAmount())
                        .shippingMethod(orderCreateCommand.getShippingMethod())
                        .build());
    }

    @Override
    public void apply(OrderCreatedEventV1 event) {
        super.apply(event);
        this.shippingMethod = ShippingMethod.HOME_DELIVERY;
    }

    public void apply(OrderCreatedEventV2 event) {
        super.apply(event);
        this.shippingMethod = event.getShippingMethod();
    }
}
