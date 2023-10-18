package com.jeremyli.orderview.handler;

import com.jeremyli.orderview.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractOrderHandler {
    final OrderService orderService;

    public AbstractOrderHandler(OrderService orderService) {
        this.orderService = orderService;
    }
}
