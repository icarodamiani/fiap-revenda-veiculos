package io.fiap.revenda.veiculos.driven.repository;

import io.fiap.revenda.veiculos.driven.domain.ImmutableDocumento;
import io.fiap.revenda.veiculos.driven.domain.ImmutablePessoa;
import io.fiap.revenda.veiculos.driven.domain.ImmutableReserva;
import io.fiap.revenda.veiculos.driven.domain.Reserva;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

@Repository
public class ReservaRepository {
    private static final String TABLE_NAME = "veiculos_reservas_tb";

    private final DynamoDbAsyncClient client;

    public ReservaRepository(DynamoDbAsyncClient client) {
        this.client = client;
    }

    public Mono<Void> save(Reserva reserva) {
        var atributos = new HashMap<String, AttributeValueUpdate>();
        atributos.put("CODIGO",
            AttributeValueUpdate.builder().value(v -> v.s(UUID.randomUUID().toString()).build()).build());
        atributos.put("VEICULO_ID",
            AttributeValueUpdate.builder().value(v -> v.s(reserva.getVeiculoId()).build()).build());
        atributos.put("VEICULO_PLACA",
            AttributeValueUpdate.builder().value(v -> v.s(reserva.getVeiculoPlaca()).build()).build());
        atributos.put("VEICULO_RENAVAM",
            AttributeValueUpdate.builder().value(v -> v.s(reserva.getVeiculoRenavam()).build()).build());
        atributos.put("RESERVADO_EM",
            AttributeValueUpdate.builder().value(v -> v.s(String.valueOf(reserva.getReservadoEm().toEpochDay()))).build());
        atributos.put("EXPIRA_EM",
            AttributeValueUpdate.builder().value(v -> v.s(String.valueOf(reserva.getExpiraEm().toEpochDay()))).build());
        atributos.put("EXPIRA_EM_TTL",
            AttributeValueUpdate.builder().value(v -> v.s(String.valueOf(reserva.getExpiraEm().toEpochDay()))).build());


        var documento = new HashMap<String, AttributeValue>();
        documento.put("VALOR", AttributeValue.builder().s(reserva.getPessoa().getDocumento().getValor()).build());
        documento.put("TIPO", AttributeValue.builder().s(reserva.getPessoa().getDocumento().getTipo()).build());

        var pessoa = new HashMap<String, AttributeValue>();
        pessoa.put("ID", AttributeValue.builder().s(reserva.getPessoa().getId()).build());
        pessoa.put("DOCUMENTO", AttributeValue.builder().m(documento).build());

        atributos.put("PESSOA",
            AttributeValueUpdate.builder().value(v -> v.m(pessoa).build()).build());

        var request = UpdateItemRequest.builder()
            .attributeUpdates(atributos)
            .tableName(TABLE_NAME)
            .key(Map.of("ID", AttributeValue.fromS(UUID.randomUUID().toString())))
            .build();

        return Mono.fromFuture(client.updateItem(request))
            .then();
    }

    public Mono<Void> deleteById(String id) {
        var key = new HashMap<String, AttributeValue>();
        key.put("ID", AttributeValue.fromS(id));

        var request = DeleteItemRequest.builder()
            .key(key)
            .tableName(TABLE_NAME)
            .build();

        return Mono.fromFuture(client.deleteItem(request))
            .then();
    }

    public Flux<Reserva> fetch() {
        return Mono.fromFuture(client.scan(ScanRequest.builder().tableName(TABLE_NAME).build()))
            .filter(ScanResponse::hasItems)
            .map(response -> response.items()
                .stream()
                .map(this::convertItem)
                .toList()
            )
            .flatMapIterable(l -> l);
    }

    public Flux<Reserva> fetchByVeiculoId(String veiculoId) {
        var request = QueryRequest.builder()
            .tableName(TABLE_NAME)
            .indexName("VeiculoIdIndex")
            .keyConditionExpression("#veiculo = :veiculo")
            .expressionAttributeNames(Map.of("#veiculo", "VEICULO_ID"))
            .expressionAttributeValues(Map.of(":veiculo", AttributeValue.fromS(veiculoId)))
            .build();

        return Mono.fromFuture(client.query(request))
            .filter(QueryResponse::hasItems)
            .flatMapIterable(QueryResponse::items)
            .map(this::convertItem);
    }

    public Flux<Reserva> fetchByCodigoReserva(String codigo) {
        var request = QueryRequest.builder()
            .tableName(TABLE_NAME)
            .indexName("CodigoReservaIndex")
            .keyConditionExpression("#codigo = :codigo")
            .expressionAttributeNames(Map.of("#codigo", "CODIGO"))
            .expressionAttributeValues(Map.of(":codigo", AttributeValue.fromS(codigo)))
            .build();

        return Mono.fromFuture(client.query(request))
            .filter(QueryResponse::hasItems)
            .flatMapIterable(QueryResponse::items)
            .map(this::convertItem);
    }

    public Flux<Reserva> fetchByVeiculo(String placa, String renavam) {
        var request = QueryRequest.builder()
            .tableName(TABLE_NAME)
            .indexName("VeiculoIndex")
            .keyConditionExpression("#placa = :placa AND #renavam = :renavam")
            .expressionAttributeNames(Map.of("#placa", "VEICULO_PLACA", "#renavam", "VEICULO_RENAVAM"))
            .expressionAttributeValues(Map.of(
                ":placa", AttributeValue.fromS(placa),
                ":renavam", AttributeValue.fromS(renavam)))
            .build();

        return Mono.fromFuture(client.query(request))
            .filter(QueryResponse::hasItems)
            .flatMapIterable(QueryResponse::items)
            .map(this::convertItem);
    }


    private Reserva convertItem(Map<String, AttributeValue> item) {
        return ImmutableReserva.builder()
            .id(item.get("ID").s())
            .veiculoId(item.get("VEICULO_ID").s())
            .veiculoPlaca(item.get("VEICULO_PLACA").s())
            .veiculoRenavam(item.get("VEICULO_RENAVAM").s())
            .veiculoId(item.get("VEICULO_ID").s())
            .codigo(item.get("CODIGO").s())
            .pessoa(ImmutablePessoa.builder()
                .id(item.get("PESSOA").m().get("ID").s())
                .documento(ImmutableDocumento.builder()
                    .tipo(item.get("PESSOA").m().get("DOCUMENTO").m().get("TIPO").s())
                    .valor(item.get("PESSOA").m().get("DOCUMENTO").m().get("VALOR").s())
                    .build()
                )
                .build())
            .reservadoEm(LocalDate.ofEpochDay(
                    Long.parseLong(item.get("RESERVADO_EM").s())
                )
            )
            .expiraEm(LocalDate.ofEpochDay(
                    Long.parseLong(item.get("EXPIRA_EM").s())
                )
            )
            .build();
    }
}
