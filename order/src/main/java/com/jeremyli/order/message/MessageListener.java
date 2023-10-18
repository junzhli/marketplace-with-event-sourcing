/* (C)2022 */
package com.jeremyli.order.message;

import com.jeremyli.common.events.*;
import com.jeremyli.common.infrastructure.TopicConstant;
import com.jeremyli.order.infrastructure.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@KafkaListener(
        id = "defaultListener-order",
        topics = {TopicConstant.TOPIC_NAME_ORDER, TopicConstant.TOPIC_NAME_ACCOUNT})
public class MessageListener {

    private final EventHandler eventHandler;

    @Autowired
    public MessageListener(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @KafkaHandler
    public void accountOrderDebitedEvent(
            AccountOrderDebitedEvent event,
            @Header(KafkaHeaders.OFFSET) int offset,
            Acknowledgment acknowledgment) {
        log.info("account topic - order debited object: {} offset: {}", event.toString(), offset);
        eventHandler.handle(event);
        acknowledgment.acknowledge();
    }

    @KafkaHandler
    public void accountOrderRejectedEvent(
            AccountOrderRejectedEvent event,
            @Header(KafkaHeaders.OFFSET) int offset,
            Acknowledgment acknowledgment) {
        log.info("account topic - order rejected object: {} offset: {}", event.toString(), offset);
        eventHandler.handle(event);
        acknowledgment.acknowledge();
    }

    @KafkaHandler
    public void orderDebited(
            OrderDebitedEvent event,
            @Header(KafkaHeaders.OFFSET) int offset,
            Acknowledgment acknowledgment) {
        log.info("order topic - order debited object: {} offset: {}", event.toString(), offset);
        eventHandler.handle(event);
        acknowledgment.acknowledge();
    }
}
