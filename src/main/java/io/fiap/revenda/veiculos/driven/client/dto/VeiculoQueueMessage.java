package io.fiap.revenda.veiculos.driven.client.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.fge.jsonpatch.JsonPatch;
import org.immutables.value.Value;

@JsonSerialize(as = ImmutableVeiculoQueueMessage.class)
@JsonDeserialize(as = ImmutableVeiculoQueueMessage.class)
@Value.Immutable
@Value.Style(privateNoargConstructor = true, jdkOnly = true)
public abstract class VeiculoQueueMessage {
    public abstract String getId();
}