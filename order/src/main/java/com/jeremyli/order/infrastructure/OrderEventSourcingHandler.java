/* (C)2022 */
package com.jeremyli.order.infrastructure;

import com.jeremyli.common.events.BaseEvent;
import com.jeremyli.common.infrastructure.EventSourcingHandler;
import com.jeremyli.common.infrastructure.EventStore;
import com.jeremyli.common.infrastructure.SnapshotManager;
import com.jeremyli.common.outbox.OutboxEventModel;
import com.jeremyli.common.outbox.OutboxEventRepository;
import com.jeremyli.order.domain.OrderAggregate;
import com.jeremyli.order.message.KafkaConfig;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderEventSourcingHandler implements EventSourcingHandler<OrderAggregate> {
    private final SnapshotManager<OrderAggregate> snapshotManager;
    private final EventStore eventStore;
    private final OutboxEventRepository outboxEventRepository;

    @Autowired
    public OrderEventSourcingHandler(
            SnapshotManager<OrderAggregate> snapshotManager,
            EventStore eventStore,
            OutboxEventRepository outboxEventRepository) {
        this.snapshotManager = snapshotManager;
        this.eventStore = eventStore;
        this.outboxEventRepository = outboxEventRepository;
    }

    @Override
    public void save(OrderAggregate aggregate) {
        eventStore.saveEvents(
                aggregate.getId(),
                OrderAggregate.class,
                aggregate.getUncommittedChanges(),
                aggregate.getVersion());
        aggregate.markChangesAsCommitted();
    }

    @Override
    public Optional<OrderAggregate> getById(String id) {
        var aggregate = snapshotManager.getByAggregateById(id);
        Optional<List<BaseEvent>> events;
        if (aggregate.isPresent()) {
            log.info("Snapshot got id: {}, {}", id, aggregate.get().toString());
            aggregate.get().markChangesAsCommitted();
            events = eventStore.getPartialEvents(id, aggregate.get().getVersion());
            if (events.isEmpty()) {
                return aggregate;
            }
        } else {
            log.info("Snapshot not got id: {}, going to get all from db", id);
            events = eventStore.getEvents(id);
            if (events.isEmpty()) {
                return Optional.empty();
            }
            aggregate = Optional.of(new OrderAggregate());
        }

        aggregate.get().replayEvents(events.get());
        var latestVersion =
                events.get().stream().map(BaseEvent::getVersion).max(Comparator.naturalOrder());

        if (latestVersion.isEmpty()) {
            throw new IllegalStateException("Unable to determine latestVersion");
        }
        aggregate.get().setVersion(latestVersion.get());
        aggregate.get().markChangesAsCommitted();
        snapshotManager.snapshot(id, aggregate.get());
        return aggregate;
    }

    @Override
    public void republishAllEvents() {
        var aggregateIds = eventStore.getAggregateIds();
        if (aggregateIds.isEmpty()) {
            return;
        }
        for (var aggregateId : aggregateIds.get()) {
            var events = eventStore.getEvents(aggregateId);
            if (events.isEmpty()) {
                continue;
            }
            for (var event : events.get()) {
                var eventEntity =
                        OutboxEventModel.builder()
                                .topic(KafkaConfig.TOPIC_NAME_ORDER)
                                .key(event.getId())
                                .eventData(event)
                                .timeStamp(new Date())
                                .build();
                outboxEventRepository.save(eventEntity);
            }
        }
    }
}
