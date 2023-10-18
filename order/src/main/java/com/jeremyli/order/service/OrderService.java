/* (C)2022 */
package com.jeremyli.order.service;

import com.jeremyli.order.web.response.OrderInfoResponse;

public interface OrderService {
    OrderInfoResponse getOrderInfoById(String orderId);
}
