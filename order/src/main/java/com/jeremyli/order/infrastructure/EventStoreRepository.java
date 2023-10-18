/* (C)2022 */
package com.jeremyli.order.infrastructure;

import com.jeremyli.common.events.EventModel;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventStoreRepository extends CrudRepository<EventModel, String> {
    List<EventModel> findByAggregateIdentifierOrderById(String aggregateIdentifier);

    List<EventModel> findByAggregateIdentifierAndVersionGreaterThan(
            String aggregateIdentifier, int version);

    List<EventModel> findAll();
}
