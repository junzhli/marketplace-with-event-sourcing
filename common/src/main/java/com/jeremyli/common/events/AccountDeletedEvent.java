/* (C)2022 */
package com.jeremyli.common.events;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDeletedEvent extends BaseEvent {
    private String userId;

    @Override
    public String toString() {
        return "AccountDeletedEvent{" + "userId=" + userId + '}';
    }

    public AccountDeletedEvent() {}

    public AccountDeletedEvent(String userId) {
        this.userId = userId;
    }
}
