package com.faker.receita.application.pf.port.out;

import com.faker.receita.domain.model.pf.PessoaFisica;

import reactor.core.publisher.Mono;

public interface PessoaFisicaRepositoryPort {

    Mono<PessoaFisica> findByNumeroCpf(String numeroCpf);

    Mono<PessoaFisica> save(PessoaFisica pessoaFisica);

    Mono<Boolean> existsByNumeroCpf(String numeroCpf);
}
