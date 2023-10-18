/* (C)2022 */
package com.jeremyli.account.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Long> {
    Optional<Transaction> findTransactionByTransactionId(String transactionId);

    List<Transaction> findTransactionsByAccount_UserId(String userId);
}
