/* (C)2022 */
package com.jeremyli.common.infrastructure;

import com.jeremyli.common.domain.BaseAggregate;
import java.util.Optional;

public interface EventSourcingHandler<T extends BaseAggregate> {
    void save(T aggregate);

    Optional<T> getById(String id);

    void republishAllEvents();
}
