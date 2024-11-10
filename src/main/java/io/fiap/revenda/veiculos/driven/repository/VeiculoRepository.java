package io.fiap.revenda.veiculos.driven.repository;

import io.fiap.revenda.veiculos.driven.domain.ImmutableVeiculo;
import io.fiap.revenda.veiculos.driven.domain.Veiculo;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

@Repository
public class VeiculoRepository {
    private static final String TABLE_NAME = "veiculos_tb";

    private final DynamoDbAsyncClient client;

    public VeiculoRepository(DynamoDbAsyncClient client) {
        this.client = client;
    }

    public Mono<Void> save(Veiculo veiculo) {
        var atributos = new HashMap<String, AttributeValueUpdate>();
        atributos.put("COR",
            AttributeValueUpdate.builder().value(v -> v.s(veiculo.getCor()).build()).build());
        atributos.put("ANO",
            AttributeValueUpdate.builder().value(v -> v.s(veiculo.getAno()).build()).build());
        atributos.put("MARCA",
            AttributeValueUpdate.builder().value(v -> v.s(veiculo.getMarca()).build()).build());
        atributos.put("PLACA",
            AttributeValueUpdate.builder().value(v -> v.s(veiculo.getPlaca()).build()).build());
        atributos.put("CAMBIO",
            AttributeValueUpdate.builder().value(v -> v.s(veiculo.getCambio()).build()).build());
        atributos.put("RENAVAM",
            AttributeValueUpdate.builder().value(v -> v.s(veiculo.getRenavam()).build()).build());
        atributos.put("MODELO",
            AttributeValueUpdate.builder().value(v -> v.s(veiculo.getModelo()).build()).build());
        atributos.put("VALOR",
            AttributeValueUpdate.builder().value(v -> v.s(veiculo.getValor()).build()).build());
        atributos.put("VENDIDO",
            AttributeValueUpdate.builder().value(v -> v.s(veiculo.getVendido().toString()).build()).build());
        atributos.put("MOTORIZACAO",
            AttributeValueUpdate.builder().value(v -> v.s(veiculo.getMotorizacao()).build()).build());
        atributos.put("QUILOMETRAGEM",
            AttributeValueUpdate.builder().value(v -> v.s(veiculo.getQuilometragem()).build()).build());

        Map<String, String> opcionais = veiculo.getOpcionais();

        atributos.put("OPCIONAIS",
            AttributeValueUpdate.builder().value(
                    v -> v.m(
                        opcionais.keySet()
                            .stream()
                            .collect(Collectors.toMap(k -> k, k -> AttributeValue.builder().s(opcionais.get(k)).build()))
                    )
                )
                .build()
        );

        var request = UpdateItemRequest.builder()
            .attributeUpdates(atributos)
            .tableName(TABLE_NAME)
            .key(Map.of("ID", AttributeValue.fromS(veiculo.getId())))
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

    public Flux<Veiculo> fetch(Boolean vendido) {
        var request = QueryRequest.builder()
            .tableName(TABLE_NAME)
            .indexName("VendidoIndex")
            .keyConditionExpression("#vendido = :vendido")
            .expressionAttributeNames(Map.of("#vendido", "VENDIDO"))
            .expressionAttributeValues(Map.of(":vendido", AttributeValue.fromS(vendido.toString())))
            .build();

        return Mono.fromFuture(client.query(request))
            .filter(QueryResponse::hasItems)
            .map(response -> response.items()
                .stream()
                .map(this::convertItem)
                .toList()
            )
            .flatMapIterable(l -> l);
    }

    public Mono<Veiculo> fetchById(String id) {
        var request = QueryRequest.builder()
            .tableName(TABLE_NAME)
            .keyConditionExpression("#id = :id")
            .expressionAttributeNames(Map.of("#id", "ID"))
            .expressionAttributeValues(Map.of(":id", AttributeValue.fromS(id)))
            .build();

        return Mono.fromFuture(client.query(request))
            .filter(QueryResponse::hasItems)
            .map(response -> response.items().get(0))
            .map(this::convertItem);
    }

    private Veiculo convertItem(Map<String, AttributeValue> item) {
        return ImmutableVeiculo.builder()
            .id(item.get("ID").s())
            .ano(item.get("ANO").s())
            .cor(item.get("COR").s())
            .marca(item.get("MARCA").s())
            .placa(item.get("PLACA").s())
            .cambio(item.get("CAMBIO").s())
            .modelo(item.get("MODELO").s())
            .renavam(item.get("RENAVAM").s())
            .valor(item.get("VALOR").s())
            .vendido(Boolean.valueOf(item.get("VENDIDO").s()))
            .motorizacao(item.get("MOTORIZACAO").s())
            .quilometragem(item.get("QUILOMETRAGEM").s())
            .opcionais(convertMapItem().apply(item.get("OPCIONAIS").m()))
            .build();
    }

    private Function<Map<String, AttributeValue>, Map<String, String>> convertMapItem() {
        return map -> map.keySet().stream()
            .collect(Collectors.toMap(k -> k, k -> map.get(k).s()));
    }
}
