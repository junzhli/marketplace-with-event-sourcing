package com.jeremyli.orderview.handler;

import com.jeremyli.common.events.BaseEvent;
import com.jeremyli.common.events.OrderCompletedEvent;
import com.jeremyli.common.events.OrderVerifiedEvent;
import com.jeremyli.orderview.model.Order;
import com.jeremyli.orderview.model.OrderState;
import com.jeremyli.orderview.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class OrderCompletedEventHandler extends AbstractOrderHandler implements EventHandler<OrderCompletedEvent>{
    public OrderCompletedEventHandler(OrderService orderService) {
        super(orderService);
    }

    @Override
    public Flux<Boolean> handle(BaseEvent event) {
        OrderCompletedEvent _event = (OrderCompletedEvent) event;
        var orderId = _event.getId();
        return orderService
                .findByOrderId(orderId)
                .map(order -> {
                    order.setOrderState(OrderState.COMPLETED);
                    return order;
                })
                .doOnNext(order -> {
                    log.info("Order changed state to completed: {}", order.toString());
                })
                .map(orderService::save)
                .concatMap(f -> f)
                .map(f -> true)
                .doOnComplete(() -> log.info("Order object persisted"));
    }
}
