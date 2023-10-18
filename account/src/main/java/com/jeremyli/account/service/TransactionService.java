/* (C)2022 */
package com.jeremyli.account.service;

import com.jeremyli.account.domain.Account;
import com.jeremyli.account.domain.Transaction;
import com.jeremyli.account.domain.TransactionType;
import java.math.BigDecimal;

public interface TransactionService {
    Transaction createTransaction(
            String transactionId,
            Account account,
            BigDecimal amount,
            TransactionType transactionType);
}
