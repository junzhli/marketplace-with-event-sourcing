/* (C)2022 */
package com.jeremyli.common.web.response;

public interface IRestResponse<T> {
    ResponseStatus getResponseStatus();

    T getResponse();

    int getHttpStatusCode();
}
