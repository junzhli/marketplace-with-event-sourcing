/* (C)2022 */
package com.jeremyli.order.infrastructure;

import com.jeremyli.common.events.BaseEvent;
import com.jeremyli.common.events.EventModel;
import com.jeremyli.common.exceptions.ConcurrencyException;
import com.jeremyli.common.infrastructure.EventStore;
import com.jeremyli.common.outbox.OutboxEventModel;
import com.jeremyli.common.outbox.OutboxEventRepository;
import com.jeremyli.order.message.KafkaConfig;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderEventStore implements EventStore {
    //    private final EventProducer eventProducer;
    private final EventStoreRepository eventStoreRepository;

    private final OutboxEventRepository outboxEventRepository;

    @Autowired
    public OrderEventStore(
            //            EventProducer eventProducer,
            EventStoreRepository eventStoreRepository,
            OutboxEventRepository outboxEventRepository) {
        //        this.eventProducer = eventProducer;
        this.eventStoreRepository = eventStoreRepository;
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional(transactionManager = "transactionManager", isolation = Isolation.READ_COMMITTED)
    @Override
    public <T> void saveEvents(
            String aggregateId,
            Class<T> aggregateClass,
            Iterable<BaseEvent> events,
            int expectedVersion) {
        var eventStream = eventStoreRepository.findByAggregateIdentifierOrderById(aggregateId);
        if (expectedVersion != -1
                && !eventStream.isEmpty()
                && eventStream.get(eventStream.size() - 1).getVersion() != expectedVersion) {
            throw new ConcurrencyException(
                    MessageFormat.format(
                            "Excepted version: {0}, Actual version: {1}",
                            expectedVersion, eventStream.get(eventStream.size() - 1).getVersion()));
        }

        var version = expectedVersion;
        for (var event : events) {
            event.setVersion(++version);
            var eventModel =
                    EventModel.builder()
                            .timeStamp(new Date())
                            .aggregateIdentifier(aggregateId)
                            .aggregateType(aggregateClass.getTypeName())
                            .version(version)
                            .eventType(event.getClass().getTypeName())
                            .eventData(event)
                            .build();
            eventStoreRepository.save(eventModel);
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

    @Override
    public Optional<List<BaseEvent>> getEvents(String aggregateId) {
        var eventModels = eventStoreRepository.findByAggregateIdentifierOrderById(aggregateId);
        if (eventModels == null || eventModels.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(
                eventModels.stream().map(EventModel::getEventData).collect(Collectors.toList()));
    }

    @Override
    public Optional<List<BaseEvent>> getPartialEvents(String aggregateId, int offset) {
        var eventModels =
                eventStoreRepository.findByAggregateIdentifierAndVersionGreaterThan(
                        aggregateId, offset);
        if (eventModels == null || eventModels.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(
                eventModels.stream().map(EventModel::getEventData).collect(Collectors.toList()));
    }

    @Override
    public Optional<List<String>> getAggregateIds() {
        var eventModels = eventStoreRepository.findAll();
        if (eventModels == null | eventModels.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(
                eventModels.stream()
                        .map(EventModel::getAggregateIdentifier)
                        .distinct()
                        .collect(Collectors.toList()));
    }
}
