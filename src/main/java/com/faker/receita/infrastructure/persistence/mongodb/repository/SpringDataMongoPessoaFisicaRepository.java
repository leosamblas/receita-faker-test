package com.faker.receita.infrastructure.persistence.mongodb.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.faker.receita.infrastructure.persistence.mongodb.document.PessoaFisicaDocument;

import reactor.core.publisher.Mono;

public interface SpringDataMongoPessoaFisicaRepository extends ReactiveMongoRepository<PessoaFisicaDocument, String> {

    Mono<PessoaFisicaDocument> findByNumeroCpf(String numeroCpf);

    Mono<Boolean> existsByNumeroCpf(String numeroCpf);
}
