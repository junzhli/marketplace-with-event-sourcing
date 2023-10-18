/* (C)2022 */
package com.jeremyli.account.message;

import com.jeremyli.account.service.EventHandler;
import com.jeremyli.common.events.OrderCreatedEventV2;
import com.jeremyli.common.events.deprecated.OrderCreatedEventV1;
import com.jeremyli.common.infrastructure.TopicConstant;
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
        id = "defaultListener-account",
        topics = {TopicConstant.TOPIC_NAME_ORDER})
public class MessageListener {

    @Autowired
    public MessageListener(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    private final EventHandler eventHandler;

    @KafkaHandler
    public void orderCreatedEvent(
            OrderCreatedEventV1 event,
            @Header(KafkaHeaders.OFFSET) int offset,
            Acknowledgment acknowledgment) {
        log.info("order topic - created object: {} offset: {}", event.toString(), offset);

        if (OrderCreatedEventV2.class.equals(event.getClass())) {
            eventHandler.handle((OrderCreatedEventV2) event);
        } else {
            eventHandler.handle(event);
        }
        acknowledgment.acknowledge();
    }
}
