package com.faker.receita.infrastructure.persistence.mongodb.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.faker.receita.infrastructure.persistence.mongodb.document.PessoaJuridicaDocument;

import reactor.core.publisher.Mono;

public interface SpringDataMongoPessoaJuridicaRepository extends ReactiveMongoRepository<PessoaJuridicaDocument, String> {

    Mono<PessoaJuridicaDocument> findByCnpj(String cnpj);

    Mono<Boolean> existsByCnpj(String cnpj);
}
