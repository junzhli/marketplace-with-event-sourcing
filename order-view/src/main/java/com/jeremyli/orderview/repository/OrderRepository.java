package com.jeremyli.orderview.repository;

import com.jeremyli.orderview.model.Order;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, String> {
    Flux<Order> findByOrderId(Mono<String> orderId);

    Flux<Order> findByOrderIdIn(List<String> orderIds);
}
