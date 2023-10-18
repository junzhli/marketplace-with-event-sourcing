/* (C)2022 */
package com.jeremyli.account.web.controller;

import com.jeremyli.account.exception.NoAccountFoundError;
import com.jeremyli.account.web.response.MessageCode;
import com.jeremyli.common.web.response.GenericMessageResponse;
import com.jeremyli.common.web.response.IRestResponse;
import com.jeremyli.common.web.response.RestResponse;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = NoAccountFoundError.class)
    public ResponseEntity<IRestResponse> badRequestExceptionHandler(Exception exception) {
        log.info("Bad request: {}", exception.getMessage(), exception);
        IRestResponse<GenericMessageResponse> res =
                RestResponse.createBadRequestResponse(
                        new GenericMessageResponse(
                                MessageCode.INVALID_ARGUMENT, exception.getMessage()));
        return ResponseEntity.status(res.getHttpStatusCode()).body(res);
    }

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

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<IRestResponse> invalidArgumentExceptionHandler(
            MethodArgumentNotValidException exception, WebRequest webRequest) {
        List<String> errors =
                exception.getAllErrors().stream()
                        .map(
                                error ->
                                        MessageFormat.format(
                                                "field: {0} message: {1}",
                                                error.getObjectName(), error.getDefaultMessage()))
                        .collect(Collectors.toList());
        log.info("Invalid argument: {}", errors, exception);
        IRestResponse<GenericMessageResponse> res =
                RestResponse.createBadRequestResponse(
                        new GenericMessageResponse(
                                MessageCode.INVALID_ARGUMENT, "Invalid argument"));
        return ResponseEntity.status(res.getHttpStatusCode()).body(res);
    }
}
