/* (C)2022 */
package com.jeremyli.account;

import com.jeremyli.common.outbox.OutboxMonitor;
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
import org.springframework.retry.annotation.EnableRetry;

@EntityScan({"com.jeremyli.account.domain", "com.jeremyli.common.outbox"})
@EnableRetry
@SpringBootApplication
@Slf4j
public class AccountApplication {

    @Autowired
    public AccountApplication(OutboxMonitor outboxMonitor) {
        this.outboxMonitor = outboxMonitor;
    }

    private final OutboxMonitor outboxMonitor;

    private final ScheduledExecutorService oneThreadScheduler = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) {
        SpringApplication.run(AccountApplication.class, args);
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
