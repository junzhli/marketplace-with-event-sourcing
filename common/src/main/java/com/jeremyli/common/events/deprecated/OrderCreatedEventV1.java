/* (C)2022 */
package com.jeremyli.common.events.deprecated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jeremyli.common.events.BaseEvent;
import java.math.BigDecimal;
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
public class OrderCreatedEventV1 extends BaseEvent {
    @JsonIgnore private final int eventVersion = 1;

    private String orderByAccountId;
    private BigDecimal amount;
}
