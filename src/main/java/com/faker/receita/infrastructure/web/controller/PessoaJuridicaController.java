package com.faker.receita.infrastructure.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.faker.receita.application.pj.port.in.PessoaJuridicaUseCase;
import com.faker.receita.infrastructure.web.dto.pj.PessoaJuridicaResponse;
import com.faker.receita.infrastructure.web.exception.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/pj")
@Tag(name = "Pessoa Jurídica (PJ)", description = "Endpoints reativos para geração de dados fakes de Pessoa Jurídica em padrão brasileiro (suporte a CNPJ clássico e alfanumérico)")
public class PessoaJuridicaController {

    private final PessoaJuridicaUseCase pessoaJuridicaUseCase;

    public PessoaJuridicaController(PessoaJuridicaUseCase pessoaJuridicaUseCase) {
        this.pessoaJuridicaUseCase = pessoaJuridicaUseCase;
    }

    @GetMapping
    @Operation(
            summary = "Gera dados fakes de Pessoa Jurídica sem documento prévio",
            description = "Gera um conjunto completo e consistente de dados cadastrais de empresa brasileira, endereço, CNAEs, QSA e regime tributário, gerando um CNPJ válido aleatório (numérico ou novo alfanumérico).",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Dados de Pessoa Jurídica gerados com sucesso",
                            content = @Content(schema = @Schema(implementation = PessoaJuridicaResponse.class))
                    )
            }
    )
    public Mono<PessoaJuridicaResponse> generateRandom() {
        return pessoaJuridicaUseCase.generateRandom()
                .map(PessoaJuridicaResponse::fromDomain);
    }

    @GetMapping("/{cnpj}")
    @Operation(
            summary = "Gera ou busca dados de Pessoa Jurídica a partir de um CNPJ",
            description = "Valida o CNPJ informado (com ou sem máscara, tradicional ou alfanumérico) e busca no banco ou gera os dados correspondentes. Retorna 400 se o CNPJ for inválido.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Dados de Pessoa Jurídica obtidos com sucesso para o CNPJ informado",
                            content = @Content(schema = @Schema(implementation = PessoaJuridicaResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "CNPJ informado é inválido ou com formato incorreto",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    public Mono<PessoaJuridicaResponse> generateByCnpj(
            @Parameter(description = "Número do CNPJ com ou sem máscara, numérico ou alfanumérico (ex: 04740876000125 ou 04.740.876/0001-25)", example = "04740876000125")
            @PathVariable String cnpj
    ) {
        return pessoaJuridicaUseCase.getOrCreateByCnpj(cnpj)
                .map(PessoaJuridicaResponse::fromDomain);
    }
}
