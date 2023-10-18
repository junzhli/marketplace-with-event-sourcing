/* (C)2022 */
package com.jeremyli.common.events;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class AccountOrderRejectedEvent extends BaseEvent {
    private String userId;
    private String orderId;

    /* Error reasons */
    private boolean accountNotExist;
    private boolean balanceInsufficient;
    private String unknownErrorDetail;

    public static AccountOrderRejectedEventBuilder builder(String userId, String orderId) {
        return new AccountOrderRejectedEventBuilder().userId(userId).orderId(orderId);
    }
}
