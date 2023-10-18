/* (C)2022 */
package com.jeremyli.common.producer;

import com.jeremyli.common.events.BaseEvent;
import com.jeremyli.common.outbox.OutboxEventModel;
import com.jeremyli.common.outbox.OutboxEventRepository;
import java.util.Date;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.data.util.Pair;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerAwareRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;

@Slf4j
public class RollbackMessageHandler implements ConsumerAwareRecordRecoverer {
    private final String topic;

    private final OutboxEventRepository outboxEventRepository;
    private final DeadLetterPublishingRecoverer deadLetterPublishingRecoverer;

    // take consumerRecord as argument to check if this event needs to rollback. If true, returns
    // its eventType as string
    private final Function<ConsumerRecord<?, ?>, String> rollbackMessageFilter;
    // output transformation
    private final BiFunction<ConsumerRecord<?, ?>, Pair<String, Exception>, BaseEvent>
            outputMessageConverter;
    private final BiFunction<ConsumerRecord<?, ?>, Exception, String> DEFAULT_DESTINATION_RESOLVER;

    public RollbackMessageHandler(
            KafkaTemplate<String, Object> kafkaTemplate,
            String topic,
            OutboxEventRepository outboxEventRepository,
            Function<ConsumerRecord<?, ?>, String> rollbackMessageFilter,
            BiFunction<ConsumerRecord<?, ?>, Pair<String, Exception>, BaseEvent>
                    outputMessageConverter) {
        this.topic = topic;
        this.outboxEventRepository = outboxEventRepository;
        this.rollbackMessageFilter = rollbackMessageFilter;
        this.outputMessageConverter = outputMessageConverter;
        this.DEFAULT_DESTINATION_RESOLVER =
                ((consumerRecord, e) -> {
                    var interestedEventType = rollbackMessageFilter.apply(consumerRecord);
                    if (interestedEventType == null) {
                        return null;
                    }
                    return interestedEventType;
                });
        this.deadLetterPublishingRecoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate, (cr, e) -> new TopicPartition(cr.topic() + ".DLT", 0));
    }

    @Override
    public void accept(ConsumerRecord<?, ?> record, Consumer<?, ?> consumer, Exception exception) {
        log.info(
                "Recovering message key {} value {} in topic {} with partition {}",
                record.key(),
                String.valueOf(record.value()),
                record.topic(),
                record.partition());
        var interestedType = DEFAULT_DESTINATION_RESOLVER.apply(record, exception);
        if (interestedType == null) {
            log.info("interestedType is null, going to dead letter...");
            deadLetterPublishingRecoverer.accept(record, exception);
            return;
        }

        var outputEvent = outputMessageConverter.apply(record, Pair.of(interestedType, exception));
        if (outputEvent == null) {
            return;
        }

        if (outboxEventRepository == null) {
            throw new IllegalStateException(
                    "outboxEventRepository is null, but we need to publish event");
        }
        log.info("Going to publish this event {} to call for rollback", outputEvent);
        var outputEventEntity =
                OutboxEventModel.builder()
                        .key((String) record.key())
                        .topic(topic)
                        .eventData(outputEvent)
                        .timeStamp(new Date())
                        .build();
        outboxEventRepository.save(outputEventEntity);
    }
}
