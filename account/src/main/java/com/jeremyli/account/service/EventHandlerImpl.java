/* (C)2022 */
package com.jeremyli.account.service;

import static com.jeremyli.account.message.KafkaConfig.TOPIC_NAME_ACCOUNT;

import com.jeremyli.account.exception.NoAccountFoundError;
import com.jeremyli.common.events.AccountOrderDebitedEvent;
import com.jeremyli.common.events.AccountOrderRejectedEvent;
import com.jeremyli.common.events.OrderCreatedEventV2;
import com.jeremyli.common.events.deprecated.OrderCreatedEventV1;
import com.jeremyli.common.outbox.OutboxEventModel;
import com.jeremyli.common.outbox.OutboxEventRepository;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class EventHandlerImpl implements EventHandler {

    private final OutboxEventRepository outboxEventRepository;
    private final AccountService accountService;

    @Autowired
    public EventHandlerImpl(
            OutboxEventRepository outboxEventRepository, AccountService accountService) {
        this.outboxEventRepository = outboxEventRepository;
        this.accountService = accountService;
    }

    @Transactional(transactionManager = "transactionManager")
    @Override
    public void handle(OrderCreatedEventV2 event) {
        handleOrderCreatedEvent(event);
    }

    @Transactional(transactionManager = "transactionManager")
    @Override
    public void handle(OrderCreatedEventV1 event) {
        handleOrderCreatedEvent(event);
    }

    private void handleOrderCreatedEvent(OrderCreatedEventV1 event) {
        try {
            var account = accountService.getAccountByUserId(event.getOrderByAccountId());
            var currentBalance = account.getBalance();
            if (currentBalance.compareTo(event.getAmount()) < 0) {
                var outboxEvent =
                        OutboxEventModel.builder()
                                .key(event.getId())
                                .topic(TOPIC_NAME_ACCOUNT)
                                .eventData(
                                        AccountOrderRejectedEvent.builder(
                                                        event.getOrderByAccountId(), event.getId())
                                                .balanceInsufficient(true)
                                                .build())
                                .timeStamp(new Date())
                                .build();
                outboxEventRepository.save(outboxEvent);
                return;
            }
            account.setBalance(currentBalance.subtract(event.getAmount()));

            var outboxEvent =
                    OutboxEventModel.builder()
                            .key(event.getId())
                            .topic(TOPIC_NAME_ACCOUNT)
                            .eventData(
                                    new AccountOrderDebitedEvent(
                                            event.getOrderByAccountId(), event.getId()))
                            .timeStamp(new Date())
                            .build();
            outboxEventRepository.save(outboxEvent);
        } catch (NoAccountFoundError error) {
            var outboxEvent =
                    OutboxEventModel.builder()
                            .key(event.getId())
                            .topic(TOPIC_NAME_ACCOUNT)
                            .eventData(
                                    AccountOrderRejectedEvent.builder(
                                                    event.getOrderByAccountId(), event.getId())
                                            .accountNotExist(true)
                                            .build())
                            .timeStamp(new Date())
                            .build();
            outboxEventRepository.save(outboxEvent);
        } catch (Exception error) {
            log.error("Handle event {} error {}", event.getClass().getTypeName(), error);
            var outboxEvent =
                    OutboxEventModel.builder()
                            .key(event.getId())
                            .topic(TOPIC_NAME_ACCOUNT)
                            .eventData(
                                    AccountOrderRejectedEvent.builder(
                                                    event.getOrderByAccountId(), event.getId())
                                            .unknownErrorDetail(error.getMessage())
                                            .build())
                            .timeStamp(new Date())
                            .build();
            outboxEventRepository.save(outboxEvent);
        }
    }
}
