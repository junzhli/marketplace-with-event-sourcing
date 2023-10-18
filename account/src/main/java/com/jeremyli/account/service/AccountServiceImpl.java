/* (C)2022 */
package com.jeremyli.account.service;

import static com.jeremyli.account.message.KafkaConfig.TOPIC_NAME_ACCOUNT;

import com.jeremyli.account.domain.Account;
import com.jeremyli.account.domain.AccountRepository;
import com.jeremyli.account.domain.TransactionType;
import com.jeremyli.account.exception.InsufficientBalanceError;
import com.jeremyli.account.exception.NoAccountFoundError;
import com.jeremyli.common.events.AccountCreatedEvent;
import com.jeremyli.common.events.AccountDeletedEvent;
import com.jeremyli.common.events.AccountTransferredEvent;
import com.jeremyli.common.outbox.OutboxEventModel;
import com.jeremyli.common.outbox.OutboxEventRepository;
import java.math.BigDecimal;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class AccountServiceImpl implements AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountRepository accountRepository;

    private final OutboxEventRepository outboxEventRepository;

    private final TransactionService transactionService;

    @Autowired
    public AccountServiceImpl(
            AccountRepository accountRepository,
            OutboxEventRepository outboxEventRepository,
            TransactionService transactionService) {
        this.accountRepository = accountRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.transactionService = transactionService;
    }

    @Override
    public Optional<Account> getAccountByName(String fullName) {
        return accountRepository.findAccountByFullName(fullName);
    }

    @Override
    public Account getAccountByUserId(String userId) {
        var account = accountRepository.findAccountByUserId(userId);
        if (account.isEmpty()) {
            throw new NoAccountFoundError(userId);
        }
        return account.get();
    }

    @Override
    @Transactional(transactionManager = "transactionManager")
    public Account createAccount(String userId, String fullName, BigDecimal balance) {
        log.info("Transaction isActive: " + TransactionSynchronizationManager.isActualTransactionActive());
        log.info("Transaction name: " + TransactionSynchronizationManager.getCurrentTransactionName());

        Account account = new Account();
        if (userId == null || userId.isEmpty()) {
            userId = UUID.randomUUID().toString();
            log.info("No userId provided. Given {}", userId);
        }
        account.setUserId(userId);
        account.setFullName(fullName);
        account.setBalance(balance);
        Account saved = accountRepository.save(account);

        log.info("Transaction isActive: " + TransactionSynchronizationManager.isActualTransactionActive());
        log.info("Transaction name: " + TransactionSynchronizationManager.getCurrentTransactionName());

        var outboxEvent =
                OutboxEventModel.builder()
                        .key(String.valueOf(userId))
                        .topic(TOPIC_NAME_ACCOUNT)
                        .eventData(new AccountCreatedEvent(userId))
                        .timeStamp(new Date())
                        .build();
        outboxEventRepository.save(outboxEvent);
        return saved;
    }

    @Override
    @Transactional(transactionManager = "transactionManager")
    public void deleteAccount(String userId) {
        Optional<Account> account = accountRepository.findAccountByUserId(userId);
        if (account.isEmpty()) {
            return;
        }
        accountRepository.delete(account.get());
        var outboxEvent =
                OutboxEventModel.builder()
                        .key(String.valueOf(userId))
                        .topic(TOPIC_NAME_ACCOUNT)
                        .eventData(new AccountDeletedEvent(userId))
                        .timeStamp(new Date())
                        .build();
        outboxEventRepository.save(outboxEvent);
    }

    @Retryable(include = CannotAcquireLockException.class, maxAttempts = 5)
    @Override
    @Transactional(transactionManager = "transactionManager")
    public void transferFundsBetweenAccounts(String userIdA, String userIdB, BigDecimal delta) {
        List<Account> accounts =
                accountRepository.findAccountsByUserIdWithPessimisticWriteLock(Arrays.asList(userIdA, userIdB));
        if (accounts.size() == 0) {
            log.error("account {} {} not found", userIdA, userIdB);
            throw new NoAccountFoundError(userIdA + " " + userIdB);
        }

        if (accounts.size() == 1) {
            var account = accounts.get(0);
            log.error("account {} not found", account.getUserId());
            throw new NoAccountFoundError(account.getUserId());
        }

        var accountA = accounts.stream().filter(a -> a.getUserId().equals(userIdA)).toArray(Account[]::new)[0];
        var accountB = accounts.stream().filter(a -> a.getUserId().equals(userIdB)).toArray(Account[]::new)[0];

        BigDecimal userABalance = accountA.getBalance();
        if (userABalance.compareTo(delta) < 0) {
            log.error("account {} has insufficient balance for transaction", userIdA);
            throw new InsufficientBalanceError(userIdA);
        }

        BigDecimal userBBalance = accountB.getBalance();
        accountA.setBalance(userABalance.subtract(delta));
        accountB.setBalance(userBBalance.add(delta));

        var transactionId = UUID.randomUUID().toString();
        transactionService.createTransaction(
                transactionId,
                accountA,
                delta.multiply(BigDecimal.ONE),
                TransactionType.ACCOUNT);
        transactionService.createTransaction(
                transactionId, accountB, delta, TransactionType.ACCOUNT);

        var outboxEvent =
                OutboxEventModel.builder()
                        .key(String.valueOf(userIdA))
                        .topic(TOPIC_NAME_ACCOUNT)
                        .eventData(new AccountTransferredEvent(userIdA, userIdB, delta))
                        .timeStamp(new Date())
                        .build();
        outboxEventRepository.save(outboxEvent);
    }
}
