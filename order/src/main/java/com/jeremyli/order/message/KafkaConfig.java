/* (C)2022 */
package com.jeremyli.order.message;

import static java.lang.String.join;
import static org.springframework.kafka.support.mapping.AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME;
import static org.springframework.kafka.support.serializer.JsonSerializer.TYPE_MAPPINGS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeremyli.common.events.*;
import com.jeremyli.common.events.deprecated.OrderCreatedEventV1;
import com.jeremyli.common.infrastructure.TopicConstant;
import com.jeremyli.common.outbox.DefaultOutboxMonitorCronjob;
import com.jeremyli.common.outbox.OutboxEventRepository;
import com.jeremyli.common.outbox.OutboxMonitor;
import com.jeremyli.common.producer.DefaultEventPublisher;
import com.jeremyli.common.producer.EventProducer;
import com.jeremyli.common.producer.RollbackMessageHandler;
import com.jeremyli.order.infrastructure.DBConfig;
import com.jeremyli.order.infrastructure.EventHandler;
import java.io.IOException;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.IsolationLevel;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

@Import(DBConfig.class)
@EnableKafka
@Configuration
@Slf4j
public class KafkaConfig {
    @Autowired
    public KafkaConfig(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    private final EventHandler eventHandler;
    public static final String TOPIC_NAME_ORDER = TopicConstant.TOPIC_NAME_ORDER;

    private static final Set<String> interestedEventTypes =
            Set.copyOf(
                    List.of(
                            EventMappings.classToTypes.get(AccountOrderDebitedEvent.class),
                            EventMappings.classToTypes.get(AccountOrderRejectedEvent.class),
                            EventMappings.classToTypes.get(OrderDebitedEvent.class)));

    @Value("${spring.kafka.bootstrap-servers}")
    private String brokers;

    /** Misc * */
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

    /** Consumer * */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-group-order-order");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(
                ConsumerConfig.ISOLATION_LEVEL_CONFIG,
                IsolationLevel.READ_COMMITTED.toString().toLowerCase());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory(
                props, new StringDeserializer(), new ByteArrayDeserializer());
    }

    @Bean
    KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>>
            kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setMessageConverter(converter());
        factory.setCommonErrorHandler(defaultErrorhandler(kafkaTemplate()));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setAckDiscarded(true);
        factory.setRecordFilterStrategy(
                consumerRecord -> {
                    if (getInterestedEventType(consumerRecord).isPresent()) {
                        return false;
                    }
                    log.info(
                            "Message from topic {} filtered at offset {} in partition {}",
                            consumerRecord.topic(),
                            consumerRecord.offset(),
                            consumerRecord.partition());
                    return true;
                });
        return factory;
    }

    @Bean
    public RecordMessageConverter converter() {
        JsonMessageConverter converter = new JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);
        typeMapper.addTrustedPackages("*");
        Map<String, Class<?>> mappings = new HashMap<>();
        mappings.put(
                EventMappings.classToTypes.get(AccountOrderDebitedEvent.class),
                AccountOrderDebitedEvent.class);
        mappings.put(
                EventMappings.classToTypes.get(AccountOrderRejectedEvent.class),
                AccountOrderRejectedEvent.class);
        mappings.put(
                EventMappings.classToTypes.get(OrderDebitedEvent.class), OrderDebitedEvent.class);
        typeMapper.setIdClassMapping(mappings);
        converter.setTypeMapper(typeMapper);
        return converter;
    }

    /** Producer * */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "tran-id-1" + UUID.randomUUID());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        List<String> typeMappings =
                Arrays.asList(
                        EventMappings.getProducerTypeToClassNameByClass(OrderCreatedEventV1.class),
                        EventMappings.getProducerTypeToClassNameByClass(OrderCreatedEventV2.class),
                        EventMappings.getProducerTypeToClassNameByClass(OrderVerifiedEvent.class),
                        EventMappings.getProducerTypeToClassNameByClass(OrderCompletedEvent.class),
                        EventMappings.getProducerTypeToClassNameByClass(OrderCanceledEvent.class),
                        EventMappings.getProducerTypeToClassNameByClass(OrderDebitedEvent.class));
        props.put(TYPE_MAPPINGS, join(",", typeMappings));
        Map<String, String> jsonSerializerConfigures = new HashMap<>();
        jsonSerializerConfigures.put(TYPE_MAPPINGS, String.join(",", typeMappings));
        JsonSerializer jsonSerializer = new JsonSerializer();
        jsonSerializer.configure(jsonSerializerConfigures, false);
        ProducerFactory<String, Object> producerFactory =
                new DefaultKafkaProducerFactory<>(props, new StringSerializer(), jsonSerializer);

        return producerFactory;
    }

    /** Event producer */
    @Bean
    public EventProducer<BaseEvent> eventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        return new DefaultEventPublisher<>(kafkaTemplate);
    }

    /** Transactional Outbox monitor */
    @Bean
    public OutboxMonitor outboxMonitor(
            OutboxEventRepository outboxEventRepository, EventProducer eventProducer) {
        return new DefaultOutboxMonitorCronjob(outboxEventRepository, eventProducer);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory());
        template.setTransactionIdPrefix("tx-" + UUID.randomUUID());
        return template;
    }

    @Bean
    public DefaultErrorHandler defaultErrorhandler(KafkaTemplate<String, Object> kafkaTemplate) {
        return new DefaultErrorHandler(
                new RollbackMessageHandler(
                        kafkaTemplate,
                        TopicConstant.TOPIC_NAME_ORDER,
                        null,
                        (consumerRecord) -> {
                            var interestedType = getInterestedEventType(consumerRecord);
                            return interestedType.orElse(null);
                        },
                        ((consumerRecord, interestedTypeWithException) -> {
                            log.info("Rollback output message converter started");
                            log.info(
                                    "Exception log for this recovering event",
                                    interestedTypeWithException.getSecond());
                            var objectMapper = new ObjectMapper();
                            var interestedType = interestedTypeWithException.getFirst();
                            if (!EventMappings.types.contains(interestedType)) {
                                return null;
                            }
                            log.warn(
                                    "Try to handle interested type {} using rollbackMessageHandler",
                                    interestedType);
                            try {
                                if (interestedType.equals(
                                        EventMappings.classToTypes.get(
                                                AccountOrderDebitedEvent.class))) {
                                    var accountOrderDebitedEvent =
                                            objectMapper.readValue(
                                                    (byte[]) consumerRecord.value(),
                                                    AccountOrderDebitedEvent.class);
                                    eventHandler.handle(accountOrderDebitedEvent);
                                } else if (interestedType.equals(
                                        EventMappings.classToTypes.get(
                                                AccountOrderRejectedEvent.class))) {
                                    var accountOrderRejectedEvent =
                                            objectMapper.readValue(
                                                    (byte[]) consumerRecord.value(),
                                                    AccountOrderRejectedEvent.class);
                                    eventHandler.handle(accountOrderRejectedEvent);
                                } else if (interestedType.equals(
                                        EventMappings.classToTypes.get(OrderDebitedEvent.class))) {
                                    var orderDebitedEvent =
                                            objectMapper.readValue(
                                                    (byte[]) consumerRecord.value(),
                                                    OrderDebitedEvent.class);
                                    eventHandler.handle(orderDebitedEvent);
                                } else {
                                    throw new IllegalStateException(
                                            "Non implemented interested type: "
                                                    + interestedTypeWithException.getFirst());
                                }
                                return null;
                            } catch (IOException e) {
                                log.error(
                                        "Error occurred when deserialize consumer record value", e);
                                throw new RuntimeException(e);
                            }
                        })),
                new FixedBackOff(1000L, 2L));
    }

    @Bean
    public NewTopic orderTopic() {
        return TopicBuilder.name(TOPIC_NAME_ORDER).partitions(2).replicas(1).build();
    }

    @Bean
    public NewTopic accountDltTopic() {
        return TopicBuilder.name(TOPIC_NAME_ORDER + ".DLT").partitions(1).replicas(1).build();
    }
}
