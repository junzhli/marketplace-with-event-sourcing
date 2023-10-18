/* (C)2022 */
package com.jeremyli.common.domain;

import com.jeremyli.common.events.BaseEvent;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
@Getter
@ToString
public abstract class BaseAggregate {
    private final int aggregateVersion = -1; // aggregate version
    private int version; // sequence number
    private String id;

    private List<BaseEvent> uncommittedChanges = new ArrayList<>();

    // subclass should implement method apply corresponding with event types
    private void applyChange(BaseEvent event, Boolean isNewEvent) {
        try {
            var method = getMethod(this.getClass(), "apply", event.getClass());
            if (method.isEmpty()) {
                throw new NoSuchMethodException();
            }
            method.get().setAccessible(true);
            method.get().invoke(this, event);
        } catch (NoSuchMethodException e) {
            log.error(
                    MessageFormat.format(
                            "No apply method signature matches the event type: {0}",
                            event.getClass().getName()));
        } catch (Exception e) {
            log.error(
                    MessageFormat.format(
                            "Error occurred when applying event: {0}", event.getClass().getName()),
                    e);
        } finally {
            if (isNewEvent) {
                uncommittedChanges.add(event);
            }
        }
    }

    public void raiseEvent(BaseEvent event) {
        applyChange(event, true);
    }

    public void replayEvents(Iterable<BaseEvent> events) {
        events.forEach(event -> applyChange(event, false));
    }

    public void markChangesAsCommitted() {
        this.uncommittedChanges.clear();
    }

    private Optional<Method> getMethod(Class<?> klass, String name, Class<?> signature) {
        if (klass == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(klass.getDeclaredMethod("apply", signature));
        } catch (NoSuchMethodException exception) {
            var superKlass = klass.getSuperclass();
            return getMethod(superKlass, name, signature);
        }
    }
}
