/* (C)2022 */
package com.jeremyli.order.web.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jeremyli.common.events.ShippingMethod;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderCreationRequest {
    @JsonProperty("id")
    @NotNull(message = "orderId must not be null")
    private String orderId;

    @JsonProperty("accountId")
    @NotNull(message = "accountId must not be null")
    private String orderByAccountId;

    @NotNull(message = "amount must not be null")
    @Min(value = 0, message = "balance must be > 0")
    private BigDecimal amount;

    @NotNull(message = "shipping method must not be null")
    private ShippingMethod shippingMethod;
}
