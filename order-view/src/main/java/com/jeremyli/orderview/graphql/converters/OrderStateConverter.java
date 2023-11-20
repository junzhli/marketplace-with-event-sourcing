package com.jeremyli.orderview.graphql.converters;

import com.jeremyli.orderview.graphql.types.OrderState;

public class OrderStateConverter implements Converter<OrderState, com.jeremyli.orderview.model.OrderState> {
    @Override
    public OrderState convert(com.jeremyli.orderview.model.OrderState t) {
        return OrderState.valueOf(t.toString());
    }
}
