/* (C)2022 */
package com.jeremyli.order.commands;

import com.jeremyli.common.commands.BaseCommand;
import com.jeremyli.common.events.ShippingMethod;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class OrderCreateCommand extends BaseCommand {
    private String id;
    private String orderByAccountId;
    private BigDecimal amount;
    private ShippingMethod shippingMethod;
}
