package io.fiap.revenda.veiculos.driven.domain.mapper;

import io.fiap.revenda.veiculos.driven.domain.Reserva;
import io.fiap.revenda.veiculos.driver.controller.dto.ReservaDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PessoaMapper.class, DocumentoMapper.class})
public interface ReservaMapper extends BaseMapper<ReservaDTO, Reserva> {
}
