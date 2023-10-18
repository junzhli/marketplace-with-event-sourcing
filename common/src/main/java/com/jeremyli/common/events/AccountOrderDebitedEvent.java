/* (C)2022 */
package com.jeremyli.common.events;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountOrderDebitedEvent extends BaseEvent {
    private String userId;
    private String orderId;
}
