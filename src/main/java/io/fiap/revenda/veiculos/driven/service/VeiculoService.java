package io.fiap.revenda.veiculos.driven.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fiap.revenda.veiculos.driven.client.SqsMessageClient;
import io.fiap.revenda.veiculos.driven.client.dto.VeiculoQueueMessage;
import io.fiap.revenda.veiculos.driven.domain.ImmutableVeiculo;
import io.fiap.revenda.veiculos.driven.domain.Reserva;
import io.fiap.revenda.veiculos.driven.domain.Veiculo;
import io.fiap.revenda.veiculos.driven.exception.TechnicalException;
import io.fiap.revenda.veiculos.driven.repository.VeiculoRepository;
import io.vavr.Function2;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

@Service
public class VeiculoService {
    private final String queue;
    private final ReservaService reservaService;
    private final VeiculoRepository repository;
    private final SqsMessageClient messageClient;
    private final ObjectMapper objectMapper;

    public VeiculoService(@Value("${aws.sqs.veiculosConfirmarVenda.queue}")
                          String queue,
                          ReservaService reservaService,
                          VeiculoRepository repository,
                          SqsMessageClient messageClient,
                          ObjectMapper objectMapper) {
        this.reservaService = reservaService;
        this.repository = repository;
        this.messageClient = messageClient;
        this.queue = queue;
        this.objectMapper = objectMapper;
    }

    public Mono<Void> save(Veiculo pessoa) {
        return repository.save(pessoa);
    }

    public Mono<Void> deleteById(String id) {
        return repository.deleteById(id);
    }

    public Flux<Veiculo> fetch(Boolean vendido) {
        return Mono.just(vendido)
            .filter(Boolean::booleanValue)
            .flatMapMany(repository::fetch)
            .switchIfEmpty(
                Flux.defer(() -> reservaService.fetch()
                    .collect(Collectors.toMap(Reserva::getVeiculoId, Reserva::getExpiraEm))
                    .flatMap(reservas ->
                        repository.fetch(vendido)
                            .filter(veiculo -> filterReservados().apply(veiculo, reservas))
                            .collectList()
                    )
                    .flatMapIterable(v -> {
                            v.sort(Comparator.comparing(o -> new BigDecimal(o.getValor())));
                            return v;
                        }
                    ))
            );
    }

    private Function2<Veiculo, Map<String, LocalDate>, Boolean> filterReservados() {
        return (veiculo, reservas) -> !reservas.containsKey(veiculo.getId()) ||
            reservas.get(veiculo.getId()).isBefore(LocalDate.now());
    }

    public Mono<Veiculo> fetchById(String id) {
        return repository.fetchById(id);
    }

    public Flux<DeleteMessageResponse> handleVeiculoConfirmacaoVenda() {
        return messageClient.receive(queue)
            .filter(ReceiveMessageResponse::hasMessages)
            .flatMapIterable(ReceiveMessageResponse::messages)
            .flatMap(message ->
                Mono.fromSupplier(() -> {
                        try {
                            return objectMapper.readValue(message.body(), VeiculoQueueMessage.class);
                        } catch (JsonProcessingException e) {
                            throw new TechnicalException("Falha ao converter mensagem de atualização de veículo.", e);
                        }
                    }).flatMap(veiculoUpdate -> this.fetchById(veiculoUpdate.getId())
                        .map(veiculo -> ImmutableVeiculo.copyOf(veiculo).withVendido(true))
                    )
                    .flatMap(this::save)
                    .flatMap(unused -> messageClient.delete(queue, message))
            );
    }
}
