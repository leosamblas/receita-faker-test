package com.faker.receita.application.pf.port.in;

import com.faker.receita.domain.model.pf.PessoaFisica;

import reactor.core.publisher.Mono;

public interface PessoaFisicaUseCase {

    /**
     * Gera uma Pessoa Física aleatória com CPF inédito válido.
     */
    Mono<PessoaFisica> generateRandom();

    /**
     * Busca no repositório por CPF ou gera e persiste se não existir.
     */
    Mono<PessoaFisica> getOrCreateByCpf(String rawOrFormattedCpf);
}
