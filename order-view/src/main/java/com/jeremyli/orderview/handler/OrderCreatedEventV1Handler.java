package com.jeremyli.orderview.handler;

import com.jeremyli.common.events.BaseEvent;
import com.jeremyli.common.events.deprecated.OrderCreatedEventV1;
import com.jeremyli.orderview.model.Order;
import com.jeremyli.orderview.model.OrderState;
import com.jeremyli.orderview.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class OrderCreatedEventV1Handler extends AbstractOrderHandler implements EventHandler<OrderCreatedEventV1>{
    public OrderCreatedEventV1Handler(OrderService orderService) {
        super(orderService);
    }

    @Override
    public Flux<Boolean> handle(BaseEvent event) {
        OrderCreatedEventV1 _event = (OrderCreatedEventV1) event;
        var newOrder = Order.builder()
                .orderId(_event.getId())
                .orderAccountId(_event.getOrderByAccountId())
                .amount(_event.getAmount())
                .orderState(OrderState.CREATED)
                .build();
        return Flux.from(orderService.createOrder(newOrder)
                .map(f -> true)
                .doOnNext(r -> {
                    log.info("Order created: {}", newOrder.toString());
                }));
    }
}
