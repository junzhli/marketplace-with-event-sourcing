/* (C)2022 */
package com.jeremyli.order.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jeremyli.order.domain.OrderAggregate;
import com.jeremyli.order.domain.OrderState;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class OrderInfoResponse {
    public OrderInfoResponse(OrderAggregate orderAggregate) {
        this.orderId = orderAggregate.getId();
        this.orderByAccountId = orderAggregate.getOrderAccountId();
        this.amount = orderAggregate.getAmount();
        this.orderState = orderAggregate.getState();
    }

    @JsonProperty("id")
    private String orderId;

    private String orderByAccountId;
    private BigDecimal amount;

    @JsonProperty("state")
    private OrderState orderState;
}
