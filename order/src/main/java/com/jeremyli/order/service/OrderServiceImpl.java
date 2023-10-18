/* (C)2022 */
package com.jeremyli.order.service;

import com.jeremyli.common.infrastructure.EventSourcingHandler;
import com.jeremyli.order.domain.OrderAggregate;
import com.jeremyli.order.exception.NoOrderFoundError;
import com.jeremyli.order.web.response.OrderInfoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    private final EventSourcingHandler<OrderAggregate> eventSourcingHandler;

    @Autowired
    public OrderServiceImpl(EventSourcingHandler<OrderAggregate> eventSourcingHandler) {
        this.eventSourcingHandler = eventSourcingHandler;
    }

    @Override
    public OrderInfoResponse getOrderInfoById(String orderId) {
        var aggregate = eventSourcingHandler.getById(orderId);
        if (aggregate.isEmpty()) {
            throw new NoOrderFoundError(orderId);
        }
        return new OrderInfoResponse(aggregate.get());
    }
}
