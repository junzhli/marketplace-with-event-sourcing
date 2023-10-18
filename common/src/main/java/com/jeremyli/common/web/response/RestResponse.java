/* (C)2022 */
package com.jeremyli.common.web.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class RestResponse<T> implements IRestResponse<T> {
    private ResponseStatus responseStatus;
    private T response;
    @JsonIgnore private int httpStatusCode;

    public static <T> RestResponse<T> createOkResponse(T response) {
        return createResponse(response, HttpStatus.OK, ResponseStatus.OK);
    }

    public static <T> RestResponse<T> createAcceptedResponse(T response) {
        return createResponse(response, HttpStatus.ACCEPTED, ResponseStatus.ACCEPTED);
    }

    public static <T> RestResponse<T> createBadRequestResponse(T response) {
        return createResponse(response, HttpStatus.BAD_REQUEST, ResponseStatus.ERROR);
    }

    public static <T> RestResponse<T> createInternalServerError(T response) {
        return createResponse(response, HttpStatus.BAD_REQUEST, ResponseStatus.ERROR);
    }

    private static <T> RestResponse<T> createResponse(
            T response, HttpStatus httpStatus, ResponseStatus responseStatus) {
        RestResponse<T> res = new RestResponse<T>();
        res.setResponse(response);
        res.setResponseStatus(responseStatus);
        res.setHttpStatusCode(httpStatus.value());
        return res;
    }
}
