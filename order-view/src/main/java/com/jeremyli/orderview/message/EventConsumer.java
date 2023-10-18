package com.jeremyli.orderview.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeremyli.common.events.BaseEvent;
import com.jeremyli.common.events.EventMappings;
import com.jeremyli.orderview.handler.EventHandlerDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.springframework.kafka.support.mapping.AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME;

@Slf4j
@Component
public class EventConsumer {

    private final Set<String> interestedEventTypes;

    private final ReactiveKafkaConsumerTemplate<String, String> consumerTemplate;

    private final EventHandlerDispatcher eventHandlerDispatcher;

    private Disposable consumer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public EventConsumer(ReactiveKafkaConsumerTemplate<String, String> consumerTemplate, @Qualifier("interestedEventTypes") Set<String> interestedEventTypes, EventHandlerDispatcher eventHandlerDispatcher) {
        this.consumerTemplate = consumerTemplate;
        this.interestedEventTypes = interestedEventTypes;
        this.eventHandlerDispatcher = eventHandlerDispatcher;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void consume() {
        consumer = consumerTemplate
                .receive()
                .groupBy(c -> ((c.key().charAt(0))) % Runtime.getRuntime().availableProcessors())
                .parallel()
                .runOn(Schedulers.parallel())
                .map(groupC -> groupC.<Mono<Tuple2<ReceiverRecord<String, String>, Boolean>>>handle((c, sink) -> {
                        log.info("Handling for data with key {}", c.key());

                        var eventTypeOpt = getInterestedEventType(c);
                        if (eventTypeOpt.isEmpty()) {
                            log.info("Skip data consuming: | key: {} value: {} partition: {} offset: {}", c.key(), c.value(), c.partition(), c.offset());
                            return;
                        }

                        var eventType = eventTypeOpt.get();
                        log.debug("Go to deserialize value with type: {}", eventType);
                        var klass = EventMappings.typeToClasses.get(eventType);

                        Object data;
                        try {
                            data = deserializeMessage(klass, c);
                        } catch (IOException e) {
                            sink.error(e);
                            return;
                        }

                        log.info("Data: | key: {} data: {} partition: {} offset: {}", c.key(), data, c.partition(), c.offset());

                        var baseEvent = (BaseEvent) klass.cast(data);
                        try {
                            var result = eventHandlerDispatcher.getEventHandler(baseEvent).handle(baseEvent);
                            sink.next(Mono.zip(Mono.just(c), Mono.from(result)));
                        } catch (Exception error) {
                            sink.error(error);
                        }
                    })
                )
                .concatMap(f -> f)
                .concatMap(f -> f)
                .doOnNext(tuple -> {
                    var o = tuple.getT1();
                    log.info("Data processed successfully: | partition: {} offset: {}", o.partition(), o.offset());
                })
                .map(tuple -> {
                    var commitOffset = tuple.getT1();
                    var parition = commitOffset.partition();
                    var offset = commitOffset.offset();
                    log.info("Record ack partition: {} offset {}", parition, offset);
                    return commitOffset.receiverOffset().commit();
                })
                .doOnNext(d -> {
                    log.info("offset commit done");
                })
                .subscribe(
                        o -> {
                            log.info("Data processed done");
                        },
                        error -> {
                            log.error("Error occurred on consuming data", error);
                        }
                );
    }

    private Optional<String> getInterestedEventType(ConsumerRecord<?, ?> consumerRecord) {
        var headers = consumerRecord.headers();
        var values = headers.headers(DEFAULT_CLASSID_FIELD_NAME);
        for (var v : values) {
            var value = new String(v.value());
            if (interestedEventTypes.contains(value)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    private <T> T deserializeMessage(Class<T> klass, ConsumerRecord<?, ?> consumerRecord) throws IOException {
        return objectMapper.readValue(
                            (byte[]) consumerRecord.value(),
                            klass);
    }

    @PreDestroy
    public void cleanup() throws InterruptedException {
        log.info("shutdown event consumer cronjob...");
        while (!consumer.isDisposed()) {
            log.info("event consumer not set to disposed... trigger dispose method and sleep for 5s");
            consumer.dispose();
            Thread.sleep(5000);
        }
        log.info("shutdown event consumer cronjob...Done");
    }
}
