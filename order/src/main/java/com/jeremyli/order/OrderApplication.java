/* (C)2022 */
package com.jeremyli.order;

import com.jeremyli.common.infrastructure.CommandDispatcher;
import com.jeremyli.common.outbox.OutboxMonitor;
import com.jeremyli.order.commands.CommandHandler;
import com.jeremyli.order.commands.OrderCreateCommand;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan({"com.jeremyli.common.events", "com.jeremyli.common.outbox"})
@Slf4j
public class OrderApplication {

    private final CommandDispatcher commandDispatcher;

    private final CommandHandler commandHandler;

    private final OutboxMonitor outboxMonitor;

    private final ScheduledExecutorService oneThreadScheduler = Executors.newScheduledThreadPool(1);

    @Autowired
    public OrderApplication(
            CommandDispatcher commandDispatcher,
            CommandHandler commandHandler,
            OutboxMonitor outboxMonitor) {
        this.commandDispatcher = commandDispatcher;
        this.commandHandler = commandHandler;
        this.outboxMonitor = outboxMonitor;
    }

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

    @PostConstruct
    public void registerCommandHandlers() {
        log.info("command handlers registering...");
        commandDispatcher.registerHandler(OrderCreateCommand.class, commandHandler::handle);
        log.info("done");
    }

    @PostConstruct
    public void runTransactionOutboxMonitor() {
        log.info(
                "outbox monitor cronjob schedule setup start... execute at periodic time interval:"
                        + " 1 second");
        oneThreadScheduler.scheduleAtFixedRate(outboxMonitor, 10L, 5L, TimeUnit.SECONDS);
        log.info("done");
    }

    @PreDestroy
    public void cleanup() throws InterruptedException {
        log.info("shutdown outbox monitor cronjob...");
        oneThreadScheduler.shutdown();
        if (!oneThreadScheduler.awaitTermination(5L, TimeUnit.SECONDS)) {
            log.info("outbox monitor didn't shut down in specific time. Forcefully shutdown...");
            oneThreadScheduler.shutdownNow();
        }
        log.info("shutdown outbox monitor cronjob...Done");
    }
}
