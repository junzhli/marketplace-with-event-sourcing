/* (C)2022 */
package com.jeremyli.order.domain.deprecated;

import com.jeremyli.common.domain.BaseAggregate;
import com.jeremyli.common.events.*;
import com.jeremyli.common.events.deprecated.OrderCreatedEventV1;
import com.jeremyli.order.commands.OrderCreateCommand;
import com.jeremyli.order.domain.OrderState;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
@Getter
@ToString(callSuper = true)
public class OrderAggregateV1 extends BaseAggregate {
    private final int aggregateVersion = 1;
    private OrderState state;
    private boolean accountBalanceDebited;
    private BigDecimal amount;
    private String orderAccountId;

    public OrderAggregateV1(OrderCreateCommand orderCreateCommand) {
        raiseEvent(
                OrderCreatedEventV1.builder()
                        .id(orderCreateCommand.getId())
                        .orderByAccountId(orderCreateCommand.getOrderByAccountId())
                        .amount(orderCreateCommand.getAmount())
                        .build());
    }

    public void setAccountBalanceDebited() {
        raiseEvent(OrderDebitedEvent.builder().id(this.getId()).build());
    }

    public void setOrderVerified() {
        if (this.state == OrderState.VERIFIED) {
            log.warn("state already set to verified orderId: {}", super.getId());
            return;
        }
        raiseEvent(OrderVerifiedEvent.builder().id(this.getId()).build());
    }

    public void setOrderCompleted() {
        if (this.state == OrderState.COMPLETED) {
            log.warn("state already set to completed orderId: {}", super.getId());
            return;
        }
        raiseEvent(OrderCompletedEvent.builder().id(this.getId()).build());
    }

    public void setOrderCanceled() {
        if (this.state == OrderState.CANCELLED) {
            log.warn("state already set to canceled orderId: {}", super.getId());
            return;
        }
        raiseEvent(OrderCanceledEvent.builder().id(this.getId()).build());
    }

    public void apply(OrderCreatedEventV1 event) {
        this.setId(event.getId());
        this.state = OrderState.CREATED;
        this.amount = event.getAmount();
        this.orderAccountId = event.getOrderByAccountId();
    }

    public void apply(OrderVerifiedEvent event) {
        this.state = OrderState.VERIFIED;
    }

    public void apply(OrderCompletedEvent event) {
        this.state = OrderState.COMPLETED;
    }

    public void apply(OrderCanceledEvent event) {
        this.state = OrderState.CANCELLED;
    }

    public void apply(OrderDebitedEvent event) {
        this.accountBalanceDebited = true;
    }

    public boolean orderReadyGetVerified() {
        return isAccountBalanceDebited();
    }
}
