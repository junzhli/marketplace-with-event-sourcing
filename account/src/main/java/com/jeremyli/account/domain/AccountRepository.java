/* (C)2022 */
package com.jeremyli.account.domain;

import java.util.List;
import java.util.Optional;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {
    Optional<Account> findAccountByFullName(String fullName);

    Optional<Account> findAccountByUserId(String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from account a where a.userId IN (:userIds)")
    List<Account> findAccountsByUserIdWithPessimisticWriteLock(
            @Param(value = "userIds") List<String> userIds);
}
