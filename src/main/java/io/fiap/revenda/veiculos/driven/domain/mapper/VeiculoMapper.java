package io.fiap.revenda.veiculos.driven.domain.mapper;

import io.fiap.revenda.veiculos.driven.domain.Veiculo;
import io.fiap.revenda.veiculos.driver.controller.dto.VeiculoDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VeiculoMapper extends BaseMapper<VeiculoDTO, Veiculo> {
}
