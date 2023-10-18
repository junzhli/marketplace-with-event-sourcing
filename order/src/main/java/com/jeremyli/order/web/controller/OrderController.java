/* (C)2022 */
package com.jeremyli.order.web.controller;

import com.jeremyli.common.infrastructure.CommandDispatcher;
import com.jeremyli.common.web.response.GenericMessageResponse;
import com.jeremyli.common.web.response.IRestResponse;
import com.jeremyli.common.web.response.RestResponse;
import com.jeremyli.order.commands.OrderCreateCommand;
import com.jeremyli.order.service.OrderService;
import com.jeremyli.order.web.request.OrderCreationRequest;
import com.jeremyli.order.web.response.MessageCode;
import com.jeremyli.order.web.response.OrderInfoResponse;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/api/orders")
public class OrderController {

    private final OrderService orderService;

    private final CommandDispatcher commandDispatcher;

    @Autowired
    public OrderController(OrderService orderService, CommandDispatcher commandDispatcher) {
        this.orderService = orderService;
        this.commandDispatcher = commandDispatcher;
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity createOrder(
            @RequestBody @Valid OrderCreationRequest orderCreationRequest) {
        var orderCreateCommand =
                OrderCreateCommand.builder()
                        .id(orderCreationRequest.getOrderId())
                        .orderByAccountId(orderCreationRequest.getOrderByAccountId())
                        .amount(orderCreationRequest.getAmount())
                        .shippingMethod(orderCreationRequest.getShippingMethod())
                        .build();

        commandDispatcher.send(orderCreateCommand);

        var res =
                RestResponse.createAcceptedResponse(
                        new GenericMessageResponse(
                                MessageCode.ASYNC_REQUEST_ACCEPTED, "Request accepted"));
        return ResponseEntity.status(res.getHttpStatusCode()).body(res);
    }

    @RequestMapping(value = "/{orderId}", method = RequestMethod.GET)
    public ResponseEntity<IRestResponse<OrderInfoResponse>> getAccount(
            @PathVariable(value = "orderId") String orderId) {
        var account = orderService.getOrderInfoById(orderId);
        var res = RestResponse.createOkResponse(account);
        return ResponseEntity.status(res.getHttpStatusCode()).body(res);
    }
}
