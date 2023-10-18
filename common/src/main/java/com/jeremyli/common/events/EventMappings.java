/* (C)2022 */
package com.jeremyli.common.events;

import com.jeremyli.common.events.deprecated.OrderCreatedEventV1;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EventMappings {
    public static final Map<String, Class<?>> typeToClasses =
            Map.ofEntries(
                    Map.entry("orderCreatedEvent", OrderCreatedEventV1.class),
                    Map.entry("orderCreatedEventV2", OrderCreatedEventV2.class),
                    Map.entry("orderVerifiedEvent", OrderVerifiedEvent.class),
                    Map.entry("orderCompletedEvent", OrderCompletedEvent.class),
                    Map.entry("orderCanceledEvent", OrderCanceledEvent.class),
                    Map.entry("orderDebitedEvent", OrderDebitedEvent.class),
                    Map.entry("accountCreatedEvent", AccountCreatedEvent.class),
                    Map.entry("accountDeletedEvent", AccountDeletedEvent.class),
                    Map.entry("accountTransferredEvent", AccountTransferredEvent.class),
                    Map.entry("accountOrderDebitedEvent", AccountOrderDebitedEvent.class),
                    Map.entry("accountOrderRejectedEvent", AccountOrderRejectedEvent.class));

    public static final Map<Class<?>, String> classToTypes =
            typeToClasses.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    public static final Set<Class<?>> classes = new HashSet<>(typeToClasses.values());

    public static final Set<String> types = new HashSet<>(typeToClasses.keySet());

    public static final String getProducerTypeToClassNameByClass(Class<?> klass) {
        if (!classes.contains(klass)) {
            throw new IllegalArgumentException("Not supported class");
        }

        return classToTypes.get(klass) + ":" + klass.getName();
    }
}
