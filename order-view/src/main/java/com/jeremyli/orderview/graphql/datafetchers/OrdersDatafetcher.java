package com.jeremyli.orderview.graphql.datafetchers;

import com.jeremyli.orderview.graphql.converters.OrderConverter;
import com.jeremyli.orderview.service.OrderService;
import com.jeremyli.orderview.graphql.types.Order;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.schema.DataFetchingEnvironment;
import reactor.core.publisher.Flux;

import java.util.List;

@DgsComponent
public class OrdersDatafetcher {
  private final OrderService orderService;

  public OrdersDatafetcher(OrderService orderService) {
    this.orderService = orderService;
  }

  @DgsData(
      parentType = "Query",
      field = "orders"
  )
  public Flux<Order> getOrders(DataFetchingEnvironment dataFetchingEnvironment, @InputArgument("ids") List<String> ids) {
    var orderConverter = new OrderConverter();
    if (ids == null || ids.isEmpty()) {
      return orderService.getAllOrders().map(orderConverter::convert);
    }
    return orderService.findByOrderIds(ids).map(orderConverter::convert);
  }
}
