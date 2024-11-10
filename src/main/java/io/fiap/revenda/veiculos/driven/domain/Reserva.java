package io.fiap.revenda.veiculos.driven.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDate;
import javax.annotation.Nullable;
import org.immutables.value.Value;

@JsonSerialize(as = ImmutableReserva.class)
@JsonDeserialize(as = ImmutableReserva.class)
@Value.Immutable
@Value.Style(privateNoargConstructor = true, jdkOnly = true)
public abstract class Reserva {
    @Nullable
    public abstract String getId();
    @Nullable
    public abstract String getCodigo();
    public abstract String getVeiculoId();
    public abstract Pessoa getPessoa();
    public abstract String getVeiculoPlaca();
    public abstract String getVeiculoRenavam();
    public abstract LocalDate getReservadoEm();
    public abstract LocalDate getExpiraEm();
}
