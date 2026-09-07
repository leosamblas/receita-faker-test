package com.faker.receita.application.pj.port.in;

import com.faker.receita.domain.model.pj.PessoaJuridica;

import reactor.core.publisher.Mono;

public interface PessoaJuridicaUseCase {

    /**
     * Gera uma Pessoa Jurídica aleatória com CNPJ inédito (numérico ou novo alfanumérico).
     */
    Mono<PessoaJuridica> generateRandom();

    /**
     * Busca no repositório por CNPJ ou gera e persiste se não existir.
     */
    Mono<PessoaJuridica> getOrCreateByCnpj(String rawOrFormattedCnpj);
}
