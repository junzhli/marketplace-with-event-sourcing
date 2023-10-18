/* (C)2022 */
package com.jeremyli.common.outbox;

import com.jeremyli.common.events.BaseEvent;
import com.jeremyli.common.producer.EventProducer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultOutboxMonitorCronjob implements OutboxMonitor {
    public DefaultOutboxMonitorCronjob(
            OutboxEventRepository outboxEventRepository, EventProducer<BaseEvent> eventProducer) {
        this.outboxEventRepository = outboxEventRepository;
        this.eventProducer = eventProducer;
    }

    private final OutboxEventRepository outboxEventRepository;

    private final EventProducer<BaseEvent> eventProducer;

    public void run() {
        try {
            log.info("OutboxMonitor cronjob started");
            var events = outboxEventRepository.findAllByOrderById();
            log.info("{} event(s) fetched", events.size());
            for (var event : events) {
                log.info(
                        "Publishing event id {} key {} data {} ...",
                        event.getId(),
                        event.getKey(),
                        event.getEventData());
                if (event.getKey() == null) {
                    eventProducer.produce(event.getTopic(), event.getEventData());
                } else {
                    eventProducer.produce(event.getTopic(), event.getKey(), event.getEventData());
                }
                outboxEventRepository.deleteById(event.getId());
            }
            log.info("OutboxMonitor cronjob end");
        } catch (Exception error) {
            log.error("Cronjob error occurred", error);
        }
    }
}
