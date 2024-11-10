package io.fiap.revenda.veiculos.driven.domain.mapper;

import io.fiap.revenda.veiculos.driven.domain.Documento;
import io.fiap.revenda.veiculos.driver.controller.dto.DocumentoDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {DocumentoMapper.class})
public interface DocumentoMapper extends BaseMapper<DocumentoDTO, Documento> {
}
