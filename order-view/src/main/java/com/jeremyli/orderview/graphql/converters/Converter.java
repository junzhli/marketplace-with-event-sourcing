package com.jeremyli.orderview.graphql.converters;

public interface Converter<A, B> {
    A convert(B t);
}
