package com.faker.receita.infrastructure.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.faker.receita.application.pf.port.in.PessoaFisicaUseCase;
import com.faker.receita.infrastructure.web.dto.pf.PessoaFisicaResponse;
import com.faker.receita.infrastructure.web.exception.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/pf")
@Tag(name = "Pessoa Física (PF)", description = "Endpoints reativos para geração de dados fakes de Pessoa Física em padrão brasileiro")
@RequiredArgsConstructor
public class PessoaFisicaController {

    private final PessoaFisicaUseCase pessoaFisicaUseCase;

    @GetMapping
    @Operation(
            summary = "Gera dados fakes de Pessoa Física sem documento prévio",
            description = "Gera um conjunto completo e consistente de dados cadastrais, endereço e contatos fakes, gerando um CPF válido aleatoriamente.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Dados de Pessoa Física gerados com sucesso",
                            content = @Content(schema = @Schema(implementation = PessoaFisicaResponse.class))
                    )
            }
    )
    public Mono<PessoaFisicaResponse> generateRandom() {
        return pessoaFisicaUseCase.generateRandom()
                .map(PessoaFisicaResponse::fromDomain);
    }

    @GetMapping("/{cpf}")
    @Operation(
            summary = "Gera dados fakes de Pessoa Física a partir de um CPF",
            description = "Valida o CPF informado (com ou sem máscara) e, sendo válido, busca no banco ou gera os dados correspondentes. Retorna 400 se o CPF for inválido.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Dados de Pessoa Física obtidos com sucesso para o CPF informado",
                            content = @Content(schema = @Schema(implementation = PessoaFisicaResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "CPF informado é inválido ou com formato incorreto",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    public Mono<PessoaFisicaResponse> generateByCpf(
            @Parameter(description = "Número do CPF com ou sem máscara (ex: 23456789012 ou 234.567.890-12)", example = "23456789012")
            @PathVariable String cpf
    ) {
        return pessoaFisicaUseCase.getOrCreateByCpf(cpf)
                .map(PessoaFisicaResponse::fromDomain);
    }
}
