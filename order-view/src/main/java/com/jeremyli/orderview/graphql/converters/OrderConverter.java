package com.jeremyli.orderview.graphql.converters;

import com.jeremyli.orderview.graphql.types.Order;

public class OrderConverter implements Converter<Order, com.jeremyli.orderview.model.Order>{
    @Override
    public Order convert(com.jeremyli.orderview.model.Order t) {
        var shippingMethodConverter = new ShippingMethodConverter();
        var shippingMethod = shippingMethodConverter.convert(t.getShippingMethod());
        var orderStateConverter = new OrderStateConverter();
        var orderState = orderStateConverter.convert(t.getOrderState());
        return Order.newBuilder()
                .id(t.getOrderId())
                .shippingMethod(shippingMethod)
                .orderState(orderState)
                .amount(t.getAmount())
                .accountId(t.getOrderAccountId())
                .build();
    }
}
