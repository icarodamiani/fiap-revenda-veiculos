package io.fiap.revenda.veiculos.driver.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Map;
import org.immutables.value.Value;

@JsonSerialize(as = ImmutableVeiculoDTO.class)
@JsonDeserialize(as = ImmutableVeiculoDTO.class)
@Value.Immutable
@Value.Style(privateNoargConstructor = true, jdkOnly = true)
public abstract class VeiculoDTO {
    public abstract String getId();
    public abstract String getAno();
    public abstract String getCor();
    public abstract String getValor();
    public abstract String getMarca();
    public abstract String getPlaca();
    public abstract String getCambio();
    public abstract String getModelo();
    public abstract String getRenavam();
    public abstract Boolean getVendido();
    public abstract String getMotorizacao();
    public abstract String getQuilometragem();
    public abstract Map<String, String> getOpcionais();
}