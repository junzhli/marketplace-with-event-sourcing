/* (C)2022 */
package com.jeremyli.order.infrastructure;

import com.jeremyli.common.infrastructure.SnapshotManager;
import com.jeremyli.order.domain.OrderAggregate;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RedisSnapshotManager implements SnapshotManager<OrderAggregate> {
    @Autowired
    public RedisSnapshotManager(
            @Qualifier(value = "orderAggregateRedisTemplate")
                    RedisTemplate<String, OrderAggregate> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private final RedisTemplate<String, OrderAggregate> redisTemplate;

    @Override
    public Optional<OrderAggregate> getByAggregateById(String aggregateId) {
        return Optional.ofNullable(this.redisTemplate.opsForValue().get(aggregateId));
    }

    @Override
    public void snapshot(String aggregateId, OrderAggregate value) {
        log.info("Snapshot.... id {} value {}", aggregateId, value);
        this.redisTemplate.opsForValue().set(aggregateId, value);
    }
}
