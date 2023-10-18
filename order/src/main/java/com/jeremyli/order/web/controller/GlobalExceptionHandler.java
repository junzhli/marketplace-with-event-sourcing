/* (C)2022 */
package com.jeremyli.order.web.controller;

import com.jeremyli.common.web.response.GenericMessageResponse;
import com.jeremyli.common.web.response.IRestResponse;
import com.jeremyli.common.web.response.RestResponse;
import com.jeremyli.order.web.response.MessageCode;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<IRestResponse> defaultErrorHandler(HttpServletRequest req, Exception e) {
        log.error("Unknown error occurred when serving request: req {} exception {}", req, e);
        IRestResponse<GenericMessageResponse> res =
                RestResponse.createInternalServerError(
                        new GenericMessageResponse(
                                MessageCode.INTERNAL_ERROR,
                                "Unknown error occurred, please try again later"));
        return ResponseEntity.status(res.getHttpStatusCode()).body(res);
    }
}
