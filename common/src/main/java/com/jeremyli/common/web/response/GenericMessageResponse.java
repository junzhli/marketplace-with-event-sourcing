/* (C)2022 */
package com.jeremyli.common.web.response;

import lombok.Getter;

@Getter
public class GenericMessageResponse {
    private int code;
    private String message;

    public GenericMessageResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public GenericMessageResponse() {}
}
