/* (C)2022 */
package com.jeremyli.common.infrastructure;

import com.jeremyli.common.events.BaseEvent;
import java.util.List;
import java.util.Optional;

public interface EventStore {
    <T> void saveEvents(
            String aggregateId, Class<T> tClass, Iterable<BaseEvent> events, int expectedVersion);

    Optional<List<BaseEvent>> getEvents(String aggregateId);

    Optional<List<BaseEvent>> getPartialEvents(String aggregateId, int offset);

    Optional<List<String>> getAggregateIds();
}
