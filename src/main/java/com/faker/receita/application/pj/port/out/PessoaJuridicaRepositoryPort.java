package com.faker.receita.application.pj.port.out;

import com.faker.receita.domain.model.pj.PessoaJuridica;

import reactor.core.publisher.Mono;

public interface PessoaJuridicaRepositoryPort {

    Mono<PessoaJuridica> findByCnpj(String cnpj);

    Mono<PessoaJuridica> save(PessoaJuridica pessoaJuridica);

    Mono<Boolean> existsByCnpj(String cnpj);
}
