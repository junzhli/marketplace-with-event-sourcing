/* (C)2022 */
package com.jeremyli.account.service;

import com.jeremyli.account.domain.Account;
import java.math.BigDecimal;
import java.util.Optional;

public interface AccountService {
    Optional<Account> getAccountByName(String fullName);

    Account getAccountByUserId(String userId);

    Account createAccount(String userId, String fullName, BigDecimal balance);

    void deleteAccount(String userId);

    void transferFundsBetweenAccounts(String userIdA, String userIdB, BigDecimal delta);
}
