package io.fiap.revenda.veiculos.driven.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fiap.revenda.veiculos.driven.client.SqsMessageClient;
import io.fiap.revenda.veiculos.driven.client.dto.PessoaExclusaoDadosMessage;
import io.fiap.revenda.veiculos.driven.domain.Reserva;
import io.fiap.revenda.veiculos.driven.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final SqsMessageClient messageClient;
    private final ObjectMapper objectMapper;
    private final String queue;

    public ReservaService(ReservaRepository reservaRepository,
                          SqsMessageClient messageClient,
                          ObjectMapper objectMapper,
                          @Value("${aws.sqs.pessoaExclusaoDados.queue}")
                          String queue) {
        this.reservaRepository = reservaRepository;
        this.messageClient = messageClient;
        this.objectMapper = objectMapper;
        this.queue = queue;
    }

    public Mono<Void> save(Reserva reserva) {
        return reservaRepository.save(reserva);
    }

    public Mono<Void> deleteById(String id) {
        return reservaRepository.deleteById(id);
    }

    public Flux<Reserva> fetch() {
        return this.fetch(null, null, null, null);
    }

    public Flux<Reserva> fetch(String veiculoId, String placa, String renavam, String codigo) {
        if (StringUtils.hasText(veiculoId)) {
            return reservaRepository.fetchByVeiculoId(veiculoId);
        } else if (StringUtils.hasText(codigo)) {
            return reservaRepository.fetchByCodigoReserva(codigo);
        } else if (StringUtils.hasText(placa) && StringUtils.hasText(renavam)) {
            return reservaRepository.fetchByVeiculo(placa, renavam);
        } else if (StringUtils.hasText(placa) || StringUtils.hasText(renavam)) {
            return Flux.error(new Exception(String.format("Filtro inválido. Veiculo ID [%s], Placa [%s], Renavam [%s]",
                veiculoId, placa, renavam)));
        }
        return reservaRepository.fetch();
    }

    public Flux<DeleteMessageResponse> handleEliminarDadosPessoais() {
        return messageClient.receive(queue)
            .filter(ReceiveMessageResponse::hasMessages)
            .flatMapIterable(ReceiveMessageResponse::messages)
            .flatMap(message ->
                Mono.fromSupplier(() -> {
                        try {
                            return objectMapper.readValue(message.body(), PessoaExclusaoDadosMessage.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }).flatMap(pessoa ->
                        this.fetch()
                            .filter(reserva -> reserva.getPessoa().getId().equals(pessoa.getId())
                                || reserva.getPessoa().getDocumento().getValor().equals(pessoa.getDocumento()))
                            .flatMap(veiculo -> deleteById(veiculo.getId()))
                            .then()
                    )
                    .then(messageClient.delete(queue, message))
            );
    }
}
