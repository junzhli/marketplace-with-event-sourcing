/* (C)2022 */
package com.jeremyli.common.infrastructure;

import java.util.Optional;

public interface SnapshotManager<T> {
    Optional<T> getByAggregateById(String aggregateId);

    void snapshot(String aggregateId, T value);
}
