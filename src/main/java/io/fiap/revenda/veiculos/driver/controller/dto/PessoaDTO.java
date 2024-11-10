package io.fiap.revenda.veiculos.driver.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fiap.revenda.veiculos.driven.domain.ImmutableReserva;
import org.immutables.value.Value;

@JsonSerialize(as = ImmutablePessoaDTO.class)
@JsonDeserialize(as = ImmutablePessoaDTO.class)
@Value.Immutable
@Value.Style(privateNoargConstructor = true, jdkOnly = true)
public abstract class PessoaDTO {
    public abstract String getId();
    public abstract DocumentoDTO getDocumento();
}