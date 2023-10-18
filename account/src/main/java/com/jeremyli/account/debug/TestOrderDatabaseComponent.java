package com.jeremyli.account.debug;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeremyli.common.events.ShippingMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;

@Component
@ConditionalOnProperty(
        value = "test2.enable",
        havingValue = "true"
)
@Slf4j
class TestOrderDatabaseComponent {
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    class OrderCreationRequest {
        @JsonProperty("id")
        @NotNull(message = "orderId must not be null")
        private String orderId;

        @JsonProperty("accountId")
        @NotNull(message = "accountId must not be null")
        private String orderByAccountId;

        @NotNull(message = "amount must not be null")
        @Min(value = 0, message = "balance must be > 0")
        private BigDecimal amount;

        @NotNull(message = "shipping method must not be null")
        private ShippingMethod shippingMethod;
    }
    private final List<String> users = Arrays.asList(
            "eb22dc12-4866-452b-b384-6a407dbea683",
            "83d91139-f13f-46f0-9bde-5bfd06852306",
            "caf6172d-8962-4cad-8d8d-55bfba7de273",
            "041f2f4e-b77e-454e-b1b0-37c8c53904e2",
            "f615f6f2-7f7d-4246-816d-f51aadad1202",
            "871247f3-3b48-49b6-9a91-cbd2985d84f3",
            "654dd003-5878-4f57-875e-313acddeddc7",
            "3c8a1f18-4bcc-4063-a714-8c16f6f5f7e3"
    );
    private final BigDecimal amount = new BigDecimal(100);
    private HttpClient createHttpClient() {
        return HttpClient
                .create()
                .baseUrl("http://localhost:8889/api/orders")
                .headers(httpHeaders -> httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    }

    private final Set<String> generatedOrderId = Collections.synchronizedSet(new HashSet<>());

    private String generateOrderId() {
        long conflictId = 0;
        String suffix = "-";
        long currentTimeMill = System.currentTimeMillis();
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(currentTimeMill);
        boolean first = true;
        int lastPos = strBuilder.length()+1; // includes "-"
        while (generatedOrderId.contains(strBuilder.toString())) {
            if (!first) {
                strBuilder.delete(lastPos, strBuilder.length());
            }
            if (first) {
                strBuilder.append(suffix);
                first = false;
            }
            strBuilder.append(conflictId++);
        }

        generatedOrderId.add(strBuilder.toString());
        return strBuilder.toString();
    }

    private HttpClient.ResponseReceiver craftPostByUserId(String userId) throws JsonProcessingException {
        var orderRequest = new OrderCreationRequest(
                generateOrderId(),
                userId,
                amount,
                ShippingMethod.HOME_DELIVERY
        );

        var objectMapper = new ObjectMapper();

        return createHttpClient()
                .post()
                .uri("/create")
                .send(ByteBufFlux.fromString(Flux.just(objectMapper.writeValueAsString(orderRequest))));
    }

    public Flux<HttpClient.ResponseReceiver<HttpClient.RequestSender>> start() {
        return Flux.<HttpClient.ResponseReceiver<HttpClient.RequestSender>>create((sink) -> {
            var random = new Random();
            var number = random.nextInt(users.size());
            var picked = users.get(number);
            try {
                sink.next(craftPostByUserId(picked));
            } catch (JsonProcessingException e) {
                sink.error(e);
            }
        });
    }

    @EventListener(ApplicationReadyEvent.class)
    public void tryApis() throws InterruptedException, JsonProcessingException {
        log.info("Start provisioning burst of traffic onto (order) services" + "| current thread: " + Thread.currentThread().getName());
        Thread.sleep(5000);

        start()
            .parallel(12)
            .runOn(Schedulers.parallel())
//                .publishOn(Schedulers.newBoundedElastic(24, Integer.MAX_VALUE, "thread-pool-custom"))
                .flatMap(f -> Flux.from(f.response()))
                .doOnEach(d -> {
                    if (d.get() == null) {
                        return;
                    }
                    int responseCode = d.get().status().code();
                    log.info("!!!! request: " + responseCode + "| current thread: " + Thread.currentThread().getName());
                })
                .sequential()
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
                }, e -> {
                    log.error("@@@@ error occurred on subscription", e);
                }, () -> {
                    log.info("@@@@ complete!!!!");
                }, subscription -> {
                    log.info("@@@@ sub request 10");
                    subscription.request(10);
                });
    }

//    private Mono<Integer> callable() {
//        return Mono.fromCallable(() -> {
//            log.info("hello");
//            return 123;
//        });
//    }

//    @EventListener(ApplicationReadyEvent.class)
//    public void helloWorld() throws InterruptedException {
//        log.info("Start provisioning burst of traffic onto (order) services" + "| current thread: " + Thread.currentThread().getName());
//        Thread.sleep(5000);
//
//        Flux.just(1,2,3,4,5,6,7,8,9,10)
//                .groupBy(c -> c % 3)
//                .parallel()
//                .runOn(Schedulers.parallel())
//                .map(g -> {
//                    return g.<Mono<Tuple2<Integer, Integer>>>handle((c, sink) -> {
//                        log.info("DDDDATA {} {}", c, Thread.currentThread().getName());
//                        if (c == 1) {
//                            return;
//                        }
//                        sink.next(Mono.zip(Mono.just(c), callable()));
//                    });
//                })
////                .<Mono<Integer>>handle((c, sink) -> {
////                    log.info("DDDDATA {}", c);
////                    if (c == 1) {
////                        return;
////                    }
////                    sink.next(Mono.just(c));
////                })
//                .concatMap(f -> f)
//                .concatMap(f -> f)
//                .map(c -> {
//                    log.info("Second layer {} {}", c.getT1(), Thread.currentThread().getName());
//                    return Mono.empty();
//                })
//                .subscribe();
//
//    }
}
