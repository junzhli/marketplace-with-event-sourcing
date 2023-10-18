/* (C)2022 */
package com.jeremyli.common.producer;

public interface EventProducer<T> {
    void produce(String topic, T event);

    void produce(String topic, String key, T event);
}
