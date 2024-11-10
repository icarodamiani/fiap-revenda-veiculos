package io.fiap.revenda.veiculos.driver.controller;

import io.fiap.revenda.veiculos.driven.domain.mapper.ReservaMapper;
import io.fiap.revenda.veiculos.driven.service.ReservaService;
import io.fiap.revenda.veiculos.driver.controller.dto.ReservaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SecurityRequirement(name = "OAuth2")
@RestController
@RequestMapping(value = "/veiculos/reservas", produces = MediaType.APPLICATION_JSON_VALUE)
public class ReservaController {

    private final ReservaService reservaService;
    private final ReservaMapper reservaMapper;

    public ReservaController(ReservaService reservaService, ReservaMapper reservaMapper) {
        this.reservaService = reservaService;
        this.reservaMapper = reservaMapper;
    }

    @PostMapping
    @Operation(description = "Cria uma nova reserva")
    public Mono<Void> save(@RequestBody ReservaDTO reserva) {
        return Mono.fromSupplier(() -> reservaMapper.domainFromDto(reserva))
            .flatMap(reservaService::save);
    }

    @DeleteMapping("/{id}")
    @Operation(description = "Deleta uma reserva por seu ID")
    public Mono<Void> deleteById(@PathVariable String id) {
        return reservaService.deleteById(id);
    }

    @GetMapping
    @Operation(description = "Busca reservas")
    public Flux<ReservaDTO> fetch(@RequestParam(required = false) String veiculoId,
                                  @RequestParam(required = false) String placa,
                                  @RequestParam(required = false) String renavam,
                                  @RequestParam(required = false) String codigo) {
        return reservaService.fetch(veiculoId, placa, renavam, codigo)
            .map(reservaMapper::dtoFromDomain);
    }
}
