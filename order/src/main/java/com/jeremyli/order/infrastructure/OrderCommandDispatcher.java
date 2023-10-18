/* (C)2022 */
package com.jeremyli.order.infrastructure;

import com.jeremyli.common.commands.BaseCommand;
import com.jeremyli.common.commands.CommandHandlerMethod;
import com.jeremyli.common.infrastructure.CommandDispatcher;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class OrderCommandDispatcher implements CommandDispatcher {
    private Map<Class<? extends BaseCommand>, List<CommandHandlerMethod>> routes = new HashMap<>();

    @Override
    public <T extends BaseCommand> void registerHandler(
            Class<T> type, CommandHandlerMethod<T> handler) {
        var handlers = routes.computeIfAbsent(type, c -> new LinkedList<>());
        handlers.add(handler);
    }

    @Override
    public void send(BaseCommand command) {
        var handlers = routes.get(command.getClass());
        if (handlers == null || handlers.size() == 0) {
            throw new IllegalArgumentException("No such command handler was registered");
        }
        handlers.forEach(x -> x.handle(command));
    }
}
