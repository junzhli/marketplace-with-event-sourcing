/* (C)2022 */
package com.jeremyli.account.service;

import com.jeremyli.account.domain.Account;
import com.jeremyli.account.domain.Transaction;
import com.jeremyli.account.domain.TransactionRepository;
import com.jeremyli.account.domain.TransactionType;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    @Override
    public Transaction createTransaction(
            String transactionId,
            Account account,
            BigDecimal amount,
            TransactionType transactionType) {
        var transaction =
                Transaction.builder()
                        .transactionId(transactionId)
                        .account(account)
                        .amount(amount)
                        .transactionType(transactionType)
                        .build();
        return transactionRepository.save(transaction);
    }
}
