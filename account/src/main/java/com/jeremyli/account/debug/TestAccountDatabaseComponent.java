package com.jeremyli.account.debug;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeremyli.account.web.request.AccountBalanceTransferRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.client.HttpClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

@Component
@ConditionalOnProperty(
        value = "test.enable",
        havingValue = "true"
)
@Slf4j
class TestAccountDatabaseComponent {
    private final String userA = "eb22dc12-4866-452b-b384-6a407dbea683";
    private final String userB = "a2c289ef-b503-406a-be2e-00d660b54d92";
    private final String userC = "8feb9a89-696b-447e-8d16-bb1be7252a57";
    private final BigDecimal amount = new BigDecimal(100);

    private HttpClient createHttpClient() {
        return HttpClient
                .create()
                .baseUrl("http://localhost:9999/api")
                .headers(httpHeaders -> httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    }

    private HttpClient.ResponseReceiver a() throws JsonProcessingException {
        var accountRequestA = new AccountBalanceTransferRequest(
                userA,
                userB,
                amount
        );

        var objectMapper = new ObjectMapper();

        return createHttpClient()
                .post()
                .uri("/accounts/balanceTransfer")
                .send(ByteBufFlux.fromString(Flux.just(objectMapper.writeValueAsString(accountRequestA))));
    }

    private HttpClient.ResponseReceiver b() throws JsonProcessingException {
        var accountRequestB = new AccountBalanceTransferRequest(
                userB,
                userC,
                amount
        );

        var objectMapper = new ObjectMapper();

        return createHttpClient()
                .post()
                .uri("/accounts/balanceTransfer")
                .send(ByteBufFlux.fromString(Flux.just(objectMapper.writeValueAsString(accountRequestB))));
    }

    private HttpClient.ResponseReceiver c() throws JsonProcessingException {
        var accountRequestC = new AccountBalanceTransferRequest(
                userC,
                userA,
                amount
        );

        var objectMapper = new ObjectMapper();

        return createHttpClient()
                .post()
                .uri("/accounts/balanceTransfer")
                .send(ByteBufFlux.fromString(Flux.just(objectMapper.writeValueAsString(accountRequestC))));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void tryApis() throws InterruptedException, JsonProcessingException {
        log.info("Start provisioning burst of traffic onto services" + "| current thread: " + Thread.currentThread().getName());
        Thread.sleep(5000);

        var pocket = Arrays.asList(a(), b(), c());
        var random = new Random();
        var list = new ArrayList<HttpClient.ResponseReceiver<HttpClient.RequestSender>>();
        for (int i = 0; i < 288; i++) {
            var number = random.nextInt(3);
            var picked = pocket.get(number);
            list.add(picked);
        }


        var flux = Flux.fromIterable(list);
        flux
//            .log()
//            .parallel(12)
//            .runOn(Schedulers.parallel())
                .publishOn(Schedulers.newBoundedElastic(24, Integer.MAX_VALUE, "thread-pool-custom"))
                .flatMap(f -> Flux.from(f.response()))
                .doOnEach(d -> {
                    if (d.get() == null) {
                        return;
                    }
                    int responseCode = d.get().status().code();
                    log.info("!!!! request: " + responseCode + "| current thread: " + Thread.currentThread().getName());
                })
//            .sequential()
                .map(d -> d.status().code())
                .groupBy(Object::toString)
                .flatMap(d -> {
                    return Mono.zip(Mono.just(d.key()), d.count());
                })
//            .subscribe(d -> {
//                log.info("@@@@ " + d);
//            });
//            .buffer(2)
//            .filter(d -> d.size() > 1)
                .subscribe(d -> {
                    log.info("@@@@ statistics for bulk requests: code: " + d.getT1() + " count: " + d.getT2() + "| current thread: " + Thread.currentThread().getName());
                });
    }
}
