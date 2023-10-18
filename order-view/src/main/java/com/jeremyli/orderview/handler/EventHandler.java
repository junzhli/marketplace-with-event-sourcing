package com.jeremyli.orderview.handler;

import com.jeremyli.common.events.BaseEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EventHandler<T extends BaseEvent> {
    Flux<Boolean> handle(BaseEvent event);
}
