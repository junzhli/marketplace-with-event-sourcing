/* (C)2022 */
package com.jeremyli.common.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.*;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jeremyli.common.events.deprecated.OrderCreatedEventV1;
import com.jeremyli.common.message.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @Type(value = OrderCreatedEventV2.class, name = "orderCreatedEventV2"), // order
    @Type(value = OrderCreatedEventV1.class, name = "orderCreatedEvent"),
    @Type(value = OrderDebitedEvent.class, name = "orderDebitedEvent"),
    @Type(value = OrderVerifiedEvent.class, name = "orderVerifiedEvent"),
    @Type(value = OrderCompletedEvent.class, name = "orderCompletedEvent"),
    @Type(value = OrderCanceledEvent.class, name = "orderCanceledEvent"),
    @Type(value = AccountTransferredEvent.class, name = "accountTransferredEvent"), // account
    @Type(value = AccountCreatedEvent.class, name = "accountCreatedEvent"),
    @Type(value = AccountDeletedEvent.class, name = "accountDeletedEvent"),
    @Type(value = AccountOrderDebitedEvent.class, name = "accountOrderDebitedEvent"),
    @Type(value = AccountOrderRejectedEvent.class, name = "accountOrderRejectedEvent"),
})
public abstract class BaseEvent extends Message {
    private final int eventVersion = -1;
    private int version;
}
