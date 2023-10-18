package com.jeremyli.orderview.message;

import com.jeremyli.common.events.*;
import com.jeremyli.common.events.deprecated.OrderCreatedEventV1;
import com.jeremyli.common.infrastructure.TopicConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.IsolationLevel;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.jeremyli.common.infrastructure.TopicConstant.TOPIC_NAME_ACCOUNT;
import static com.jeremyli.common.infrastructure.TopicConstant.TOPIC_NAME_ORDER;
import static org.springframework.kafka.support.mapping.AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME;

@Configuration
@Slf4j
public class KafkaConfig {

    private static final Set<String> interestedEventTypes =
            Set.copyOf(
                    List.of(
                            EventMappings.classToTypes.get(OrderCreatedEventV1.class),
                            EventMappings.classToTypes.get(OrderCreatedEventV2.class),
                            EventMappings.classToTypes.get(OrderVerifiedEvent.class),
                            EventMappings.classToTypes.get(OrderCompletedEvent.class),
                            EventMappings.classToTypes.get(OrderCanceledEvent.class),
                            EventMappings.classToTypes.get(OrderDebitedEvent.class)
                    ));
    private final static List<String> interestedTopics = List.of(
            TOPIC_NAME_ORDER
    );

    @Bean("interestedEventTypes")
    public Set<String> getInterestedEventTypes() {
        return interestedEventTypes;
    }

    @Bean
    public ReceiverOptions<String, String> kafkaReceiverOptions(KafkaProperties kafkaProperties) {
        Map<String, Object> config = kafkaProperties.buildConsumerProperties();
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "order-view-consumer-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(
                ConsumerConfig.ISOLATION_LEVEL_CONFIG,
                IsolationLevel.READ_COMMITTED.toString().toLowerCase());

        ReceiverOptions<String, String> basicReceiverOptions = ReceiverOptions.create(config);
        return basicReceiverOptions.subscription(interestedTopics);
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<String, String> reactiveKafkaConsumerTemplate(ReceiverOptions<String, String> kafkaReceiverOptions) {
        return new ReactiveKafkaConsumerTemplate<>(kafkaReceiverOptions);
    }
}
