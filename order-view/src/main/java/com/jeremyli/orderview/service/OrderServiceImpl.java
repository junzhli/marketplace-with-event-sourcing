package com.jeremyli.orderview.service;

import com.jeremyli.orderview.model.Order;
import com.jeremyli.orderview.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    private final OrderRepository orderRepository;

    @Override
    public Flux<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Mono<Order> createOrder(Order order) {
        return orderRepository.save(order);
    }

    public Flux<Order> findByOrderId(String orderId) {

        return orderRepository.findByOrderId(Mono.just(orderId));
    }

    public Flux<Order> findByOrderIds(List<String> orderId) {
        return orderRepository.findByOrderIdIn(orderId);
    }

    @Override
    public Mono<Order> save(Order order) {
        return orderRepository.save(order);
    }
}
