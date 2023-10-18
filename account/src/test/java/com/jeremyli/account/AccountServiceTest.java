/* (C)2022 */
package com.jeremyli.account;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jeremyli.account.domain.Account;
import com.jeremyli.account.domain.AccountRepository;
import com.jeremyli.account.service.AccountService;
import com.jeremyli.account.service.AccountServiceImpl;
import com.jeremyli.account.service.TransactionService;
import com.jeremyli.common.events.AccountCreatedEvent;
import com.jeremyli.common.outbox.OutboxEventModel;
import com.jeremyli.common.outbox.OutboxEventRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock private AccountRepository accountRepository;

    @Mock private OutboxEventRepository outboxEventRepository;

    @Captor ArgumentCaptor<OutboxEventModel> outboxEventModelArgumentCaptor;

    @Mock private TransactionService transactionService;

    private AccountService accountService;

    @BeforeEach
    public void beforeEach() {
        accountService =
                new AccountServiceImpl(
                        accountRepository, outboxEventRepository, transactionService);
    }

    @Test
    public void accountShouldBeCreated() {
        var userId = "123";
        var fullName = "hahah";
        var balance = new BigDecimal("10");
        var account = new Account();
        account.setUserId(userId);
        account.setFullName(fullName);
        account.setBalance(balance);
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        var result = accountService.createAccount(userId, fullName, balance);
        assertThat(result).usingRecursiveComparison().isEqualTo(account);
        Mockito.verify(outboxEventRepository, Mockito.times(1))
                .save(outboxEventModelArgumentCaptor.capture());
        OutboxEventModel outboxEventModel = outboxEventModelArgumentCaptor.getValue();
        assertThat(outboxEventModel.getKey()).isEqualTo(userId);
        assertThat(((AccountCreatedEvent) outboxEventModel.getEventData()).getUserId())
                .isEqualTo("123");
    }

    @Test
    public void accountShouldBeCreatedWhenUserIdNotProvided() {
        var fullName = "hahah";
        var balance = new BigDecimal("10");
        var account = new Account();
        account.setFullName(fullName);
        account.setBalance(balance);
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        var result = accountService.createAccount(null, fullName, balance);
        assertThat(result).usingRecursiveComparison().isEqualTo(account);
        Mockito.verify(outboxEventRepository, Mockito.times(1))
                .save(outboxEventModelArgumentCaptor.capture());
        OutboxEventModel outboxEventModel = outboxEventModelArgumentCaptor.getValue();
        assertThat(!outboxEventModel.getKey().isEmpty()).isEqualTo(true);
        assertThat(!((AccountCreatedEvent) outboxEventModel.getEventData()).getUserId().isEmpty())
                .isEqualTo(true);
    }
}
