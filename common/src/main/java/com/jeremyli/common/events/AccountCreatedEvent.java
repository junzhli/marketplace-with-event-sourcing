/* (C)2022 */
package com.jeremyli.common.events;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountCreatedEvent extends BaseEvent {
    private String userId;

    public AccountCreatedEvent() {}

    @Override
    public String toString() {
        return "AccountCreatedEvent{" + "userId=" + userId + '}';
    }

    public AccountCreatedEvent(String userId) {
        this.userId = userId;
    }
}
