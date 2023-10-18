/* (C)2022 */
package com.jeremyli.order.commands;

import com.jeremyli.common.infrastructure.EventSourcingHandler;
import com.jeremyli.order.domain.OrderAggregate;
import java.text.MessageFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderCommandHandler implements CommandHandler {
    private EventSourcingHandler<OrderAggregate> eventSourcingHandler;

    @Autowired
    public OrderCommandHandler(EventSourcingHandler<OrderAggregate> eventSourcingHandler) {
        this.eventSourcingHandler = eventSourcingHandler;
    }

    @Override
    public void handle(OrderCreateCommand command) {
        var orderAggregate = eventSourcingHandler.getById(command.getId());
        if (orderAggregate.isPresent()) {
            log.error(MessageFormat.format("Order {0} already created", command.getId()));
            throw new IllegalArgumentException(
                    MessageFormat.format("Already created for orderId {0}", command.getId()));
        }
        var newOrder = new OrderAggregate(command);
        eventSourcingHandler.save(newOrder);
    }
}
