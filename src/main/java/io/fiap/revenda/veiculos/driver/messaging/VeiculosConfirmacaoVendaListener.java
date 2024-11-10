package io.fiap.revenda.veiculos.driver.messaging;

import io.fiap.revenda.veiculos.driven.service.VeiculoService;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Component
public class VeiculosConfirmacaoVendaListener implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(VeiculosConfirmacaoVendaListener.class);

    private final SimpleTriggerContext triggerContext;
    private final PeriodicTrigger trigger;
    private final Scheduler boundedElastic;
    private final VeiculoService service;

    public VeiculosConfirmacaoVendaListener(@Value("${aws.sqs.veiculosVenda.delay:10000}")
                                  String delay,
                                            @Value("${aws.sqs.veiculosVenda.poolSize:1}")
                                  String poolSize,
                                            VeiculoService service) {
        this.service = service;
        boundedElastic = Schedulers.newBoundedElastic(Integer.parseInt(poolSize), 10000,
            "veiculosUpdateListenerPool", 600, true);

        this.triggerContext = new SimpleTriggerContext();
        this.trigger = new PeriodicTrigger(Duration.ofMillis(Long.parseLong(delay)));

    }

    @Override
    public void run(String... args) {
        Flux.<Duration>generate(sink -> {
                Instant instant = this.trigger.nextExecution(triggerContext);
                if (instant != null) {
                    triggerContext.update(instant, null, null);
                    long millis = instant.toEpochMilli() - System.currentTimeMillis();
                    sink.next(Duration.ofMillis(millis));
                } else {
                    sink.complete();
                }
            })
            .concatMap(duration -> Mono.delay(duration)
                    .doOnNext(l -> triggerContext.update(
                        Instant.now(),
                        triggerContext.lastActualExecution(),
                        null))
                    .flatMapMany(unused -> service.handleVeiculoConfirmacaoVenda())
                    .doOnComplete(() -> triggerContext.update(
                        triggerContext.lastScheduledExecution(),
                        triggerContext.lastActualExecution(),
                        Instant.now())
                    )
                    .doOnError(error -> LOGGER.error("an error occurred during message listener: " + error.getMessage(), error))
                , 0)
            .map(unused -> "")
            .onErrorResume(throwable -> Flux.just(""))
            .repeat()
            .subscribeOn(boundedElastic)
            .subscribe();
    }
}