package com.faker.receita.infrastructure.persistence.mongodb.adapter;

import org.springframework.stereotype.Component;

import com.faker.receita.application.pf.port.out.PessoaFisicaRepositoryPort;
import com.faker.receita.domain.model.pf.PessoaFisica;
import com.faker.receita.infrastructure.persistence.mongodb.document.PessoaFisicaDocument;
import com.faker.receita.infrastructure.persistence.mongodb.repository.SpringDataMongoPessoaFisicaRepository;

import reactor.core.publisher.Mono;

@Component
public class PessoaFisicaMongoAdapter implements PessoaFisicaRepositoryPort {

    private final SpringDataMongoPessoaFisicaRepository repository;

    public PessoaFisicaMongoAdapter(SpringDataMongoPessoaFisicaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<PessoaFisica> findByNumeroCpf(String numeroCpf) {
        return repository.findByNumeroCpf(numeroCpf)
                .map(PessoaFisicaDocument::toDomain);
    }

    @Override
    public Mono<PessoaFisica> save(PessoaFisica pessoaFisica) {
        PessoaFisicaDocument document = PessoaFisicaDocument.fromDomain(pessoaFisica);
        return repository.save(document)
                .map(PessoaFisicaDocument::toDomain);
    }

    @Override
    public Mono<Boolean> existsByNumeroCpf(String numeroCpf) {
        return repository.existsByNumeroCpf(numeroCpf);
    }
}
