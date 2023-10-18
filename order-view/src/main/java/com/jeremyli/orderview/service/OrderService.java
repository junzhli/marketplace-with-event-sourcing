package com.jeremyli.orderview.service;

import com.jeremyli.orderview.model.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {
    Flux<Order> getAllOrders();

    Mono<Order> createOrder(Order order);

    Flux<Order> findByOrderId(String orderId);

    Mono<Order> save(Order order);
}
