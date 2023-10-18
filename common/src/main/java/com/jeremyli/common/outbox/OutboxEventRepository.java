/* (C)2022 */
package com.jeremyli.common.outbox;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxEventRepository extends CrudRepository<OutboxEventModel, Long> {
    Optional<OutboxEventModel> findByOrderById();

    List<OutboxEventModel> findAllByOrderById();
}
