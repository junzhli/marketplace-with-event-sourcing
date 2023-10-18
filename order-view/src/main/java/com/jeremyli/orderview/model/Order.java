package com.jeremyli.orderview.model;

import com.jeremyli.common.events.ShippingMethod;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.math.BigDecimal;
import java.util.Date;

@Document(value = "orders")
@Builder
@Data
public class Order {
    @Id
    private String orderId;

    private OrderState orderState;

    private BigDecimal amount;

    private ShippingMethod shippingMethod;

    private String orderAccountId;

    private boolean accountBalanceDebited;

    @Version
    private Integer version;

    @CreatedDate
    private Date createdTime;

    @LastModifiedDate
    private Date updatedTime;
}
