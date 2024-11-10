package io.fiap.revenda.veiculos.driven.domain.mapper;

import io.fiap.revenda.veiculos.driven.domain.Pessoa;
import io.fiap.revenda.veiculos.driver.controller.dto.PessoaDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PessoaMapper extends BaseMapper<PessoaDTO, Pessoa> {
}
