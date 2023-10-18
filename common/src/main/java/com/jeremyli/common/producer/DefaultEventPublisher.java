/* (C)2022 */
package com.jeremyli.common.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public class DefaultEventPublisher<T> implements EventProducer<T> {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public DefaultEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    @Override
    public void produce(String topic, T event) {
        log.info("Publishing {} {} in topic {}", event.getClass().getTypeName(), event, topic);
        kafkaTemplate.send(topic, event);
    }

    @Transactional
    @Override
    public void produce(String topic, String key, T event) {
        log.info(
                "Publishing {} {} in topic {} with key {}",
                event.getClass().getTypeName(),
                event,
                topic,
                key);
        kafkaTemplate.send(topic, key, event);
    }
}
