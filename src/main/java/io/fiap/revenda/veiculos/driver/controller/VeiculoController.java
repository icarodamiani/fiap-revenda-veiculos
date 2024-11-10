package io.fiap.revenda.veiculos.driver.controller;

import io.fiap.revenda.veiculos.driven.domain.mapper.VeiculoMapper;
import io.fiap.revenda.veiculos.driven.service.VeiculoService;
import io.fiap.revenda.veiculos.driver.controller.dto.VeiculoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SecurityRequirement(name = "OAuth2")
@RestController
@RequestMapping(value = "/veiculos", produces = MediaType.APPLICATION_JSON_VALUE)
public class VeiculoController {

    private final VeiculoService veiculoService;
    private final VeiculoMapper veiculoMapper;

    public VeiculoController(VeiculoService veiculoService, VeiculoMapper veiculoMapper) {
        this.veiculoService = veiculoService;
        this.veiculoMapper = veiculoMapper;
    }

    @PostMapping
    @Operation(description = "Cria uma nova veículo")
    public Mono<Void> save(@RequestBody VeiculoDTO pessoa) {
        return Mono.fromSupplier(() -> veiculoMapper.domainFromDto(pessoa))
            .flatMap(veiculoService::save);
    }

    @DeleteMapping("/{id}")
    @Operation(description = "Deleta uma veículo por seu ID")
    public Mono<Void> deleteById(@PathVariable String id) {
        return veiculoService.deleteById(id);
    }

    @GetMapping("/vendidos")
    @Operation(description = "Busca veículos vendidos")
    public Flux<VeiculoDTO> fetchSold() {
        return veiculoService.fetch(true)
            .map(veiculoMapper::dtoFromDomain);
    }

    @GetMapping
    @Operation(description = "Busca veículos disponíveis e sem reserva")
    public Flux<VeiculoDTO> fetch() {
        return veiculoService.fetch(false)
            .map(veiculoMapper::dtoFromDomain);
    }

    @GetMapping("/{id}")
    @Operation(description = "Busca um veículo por seu ID")
    public Mono<VeiculoDTO> fetchById(@PathVariable String id) {
        return veiculoService.fetchById(id)
            .map(veiculoMapper::dtoFromDomain);
    }
}
