/* (C)2022 */
package com.jeremyli.common.events;

import com.jeremyli.common.events.deprecated.OrderCreatedEventV1;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
public class OrderCreatedEventV2 extends OrderCreatedEventV1 {
    private final int eventVersion = 2;

    private ShippingMethod shippingMethod;
}
