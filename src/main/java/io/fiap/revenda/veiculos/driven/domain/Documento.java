package io.fiap.revenda.veiculos.driven.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@JsonSerialize(as = ImmutableDocumento.class)
@JsonDeserialize(as = ImmutableDocumento.class)
@Value.Immutable
@Value.Style(privateNoargConstructor = true, jdkOnly = true)
public abstract class Documento {
    public abstract String getTipo();
    public abstract String getValor();
}