package com.jeremyli.orderview.handler;

import com.jeremyli.common.events.*;
import com.jeremyli.common.events.deprecated.OrderCreatedEventV1;
import com.jeremyli.orderview.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventHandlerConfig {
    @Autowired
    public EventHandlerConfig(OrderService orderService) {
        this.orderService = orderService;
    }

    private final OrderService orderService;

    @Bean
    public EventHandlerDispatcher eventHandlerDispatcher() {
        var dispatcher = new EventHandlerDispatcher();
        dispatcher.registerEventHandlerByClass(OrderCreatedEventV1.class, new OrderCreatedEventV1Handler(orderService));
        dispatcher.registerEventHandlerByClass(OrderCreatedEventV2.class, new OrderCreatedEventV2Handler(orderService));
        dispatcher.registerEventHandlerByClass(OrderVerifiedEvent.class, new OrderVerifiedEventHandler(orderService));
        dispatcher.registerEventHandlerByClass(OrderCompletedEvent.class, new OrderCompletedEventHandler(orderService));
        dispatcher.registerEventHandlerByClass(OrderCanceledEvent.class, new OrderCancelledEventHandler(orderService));
        dispatcher.registerEventHandlerByClass(OrderDebitedEvent.class, new OrderDebitedEventHandler(orderService));
        return dispatcher;
    }
}
