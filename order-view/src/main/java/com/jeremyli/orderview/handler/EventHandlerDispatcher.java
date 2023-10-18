package com.jeremyli.orderview.handler;

import com.jeremyli.common.events.BaseEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;

public class EventHandlerDispatcher {
    private final HashMap<Class<?>, EventHandler<?>> klassToEventHandlers = new HashMap<>();

    public <T extends BaseEvent> void registerEventHandlerByClass(Class<T> klass, EventHandler<T> eventHandler) {
        klassToEventHandlers.put(klass, eventHandler);
    }

    public EventHandler<? extends BaseEvent> getEventHandler(Object event) {
        if (!klassToEventHandlers.containsKey(event.getClass())) {
            throw new RuntimeException("the type of event handler not registered");
        }

        return klassToEventHandlers.get(event.getClass());
    }
}
