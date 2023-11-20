package com.jeremyli.orderview.graphql.converters;

import com.jeremyli.orderview.graphql.types.ShippingMethod;

public class ShippingMethodConverter implements Converter<ShippingMethod, com.jeremyli.common.events.ShippingMethod> {
    @Override
    public ShippingMethod convert(com.jeremyli.common.events.ShippingMethod t) {
        return ShippingMethod.valueOf(t.toString());
    }
}
