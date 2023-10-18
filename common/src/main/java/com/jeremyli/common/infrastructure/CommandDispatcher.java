/* (C)2022 */
package com.jeremyli.common.infrastructure;

import com.jeremyli.common.commands.BaseCommand;
import com.jeremyli.common.commands.CommandHandlerMethod;

public interface CommandDispatcher {
    <T extends BaseCommand> void registerHandler(Class<T> type, CommandHandlerMethod<T> handler);

    void send(BaseCommand command);
}
