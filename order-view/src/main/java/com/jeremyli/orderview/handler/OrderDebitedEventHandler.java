package com.jeremyli.orderview.handler;

import com.jeremyli.common.events.BaseEvent;
import com.jeremyli.common.events.OrderDebitedEvent;
import com.jeremyli.common.events.OrderVerifiedEvent;
import com.jeremyli.orderview.model.Order;
import com.jeremyli.orderview.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class OrderDebitedEventHandler extends AbstractOrderHandler implements EventHandler<OrderDebitedEvent>{
    public OrderDebitedEventHandler(OrderService orderService) {
        super(orderService);
    }

    @Override
    public Flux<Boolean> handle(BaseEvent event) {
        OrderDebitedEvent _event = (OrderDebitedEvent) event;
        var orderId = _event.getId();
        return orderService
                .findByOrderId(orderId)
                .map(order -> {
                    order.setAccountBalanceDebited(true);
                    return order;
                })
                .doOnNext(order -> {
                    log.info("Order changed state to debit verified: {}", order.toString());
                })
                .map(orderService::save)
                .concatMap(f -> f)
                .map(f -> true)
                .doOnComplete(() -> log.info("Order object persisted"));
    }
}
