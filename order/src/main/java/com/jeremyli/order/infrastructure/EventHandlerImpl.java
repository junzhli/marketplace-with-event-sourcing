/* (C)2022 */
package com.jeremyli.order.infrastructure;

import com.jeremyli.common.events.AccountOrderDebitedEvent;
import com.jeremyli.common.events.AccountOrderRejectedEvent;
import com.jeremyli.common.events.OrderDebitedEvent;
import com.jeremyli.common.infrastructure.EventSourcingHandler;
import com.jeremyli.order.domain.OrderAggregate;
import com.jeremyli.order.domain.OrderState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventHandlerImpl implements EventHandler {
    //    private final EventProducer eventProducer;
    private final EventSourcingHandler<OrderAggregate> eventSourcingHandler;

    @Autowired
    public EventHandlerImpl(
            //            EventProducer eventProducer,
            EventSourcingHandler<OrderAggregate> eventSourcingHandler) {
        //        this.eventProducer = eventProducer;
        this.eventSourcingHandler = eventSourcingHandler;
    }

    /** Account */
    @Override
    public void handle(AccountOrderDebitedEvent event) {
        var aggregate = eventSourcingHandler.getById(event.getOrderId());
        if (aggregate.isEmpty()) {
            log.error("No orderId {} found, ignored", event.getOrderId());
            return;
        }

        if (aggregate.get().isAccountBalanceDebited()) {
            log.warn(
                    "OrderId {} found, skipped the event due to the fact that"
                            + " accountBalanceDebited is true",
                    event.getOrderId());
            return;
        }
        aggregate.get().setAccountBalanceDebited();

        eventSourcingHandler.save(aggregate.get());
    }

    @Override
    public void handle(AccountOrderRejectedEvent event) {
        var aggregate = eventSourcingHandler.getById(event.getOrderId());
        if (aggregate.isEmpty()) {
            log.error("No orderId {} found, ignored", event.getOrderId());
            return;
        }

        if (aggregate.get().getState() == OrderState.CANCELLED) {
            log.warn(
                    "OrderId {} found, skipped the event due to the fact that the state is"
                            + " cancelled now",
                    event.getOrderId());
            return;
        }
        aggregate.get().setOrderCanceled();

        eventSourcingHandler.save(aggregate.get());
    }

    /** Order */
    @Override
    public void handle(OrderDebitedEvent event) {
        var aggregate = eventSourcingHandler.getById(event.getId());
        if (aggregate.isEmpty()) {
            log.error("No orderId {} found, ignored", event.getId());
            return;
        }

        if (!aggregate.get().orderReadyGetVerified()) {
            log.warn("No orderId {} found, not ready to get verified", event.getId());
            return;
        }

        aggregate.get().setOrderVerified();

        eventSourcingHandler.save(aggregate.get());
    }
}
