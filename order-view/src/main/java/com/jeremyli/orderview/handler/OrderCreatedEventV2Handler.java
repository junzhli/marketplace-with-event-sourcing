package com.jeremyli.orderview.handler;

import com.jeremyli.common.events.BaseEvent;
import com.jeremyli.common.events.OrderCreatedEventV2;
import com.jeremyli.orderview.model.Order;
import com.jeremyli.orderview.model.OrderState;
import com.jeremyli.orderview.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class OrderCreatedEventV2Handler extends AbstractOrderHandler implements EventHandler<OrderCreatedEventV2>{
    public OrderCreatedEventV2Handler(OrderService orderService) {
        super(orderService);
    }

    @Override
    public Flux<Boolean> handle(BaseEvent event) {
        OrderCreatedEventV2 _event = (OrderCreatedEventV2) event;
        var newOrder = Order.builder()
                .orderId(_event.getId())
                .orderAccountId(_event.getOrderByAccountId())
                .amount(_event.getAmount())
                .orderState(OrderState.CREATED)
                .shippingMethod(_event.getShippingMethod())
                .build();
        return Flux.from(orderService
                .createOrder(newOrder)
                .map(f -> true)
                .doOnNext(r -> {
                    log.info("Order v2 created: {}", newOrder.toString());
                }));
    }
}
