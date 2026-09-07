package com.faker.receita.infrastructure.persistence.mongodb.adapter;

import org.springframework.stereotype.Component;

import com.faker.receita.application.pj.port.out.PessoaJuridicaRepositoryPort;
import com.faker.receita.domain.model.pj.PessoaJuridica;
import com.faker.receita.infrastructure.persistence.mongodb.document.PessoaJuridicaDocument;
import com.faker.receita.infrastructure.persistence.mongodb.repository.SpringDataMongoPessoaJuridicaRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class PessoaJuridicaMongoAdapter implements PessoaJuridicaRepositoryPort {

    private final SpringDataMongoPessoaJuridicaRepository repository;

    @Override
    public Mono<PessoaJuridica> findByCnpj(String cnpj) {
        return repository.findByCnpj(cnpj)
                .map(pj -> pj.toDomain());
    }

    @Override
    public Mono<PessoaJuridica> save(PessoaJuridica pessoaJuridica) {
        PessoaJuridicaDocument document = PessoaJuridicaDocument.fromDomain(pessoaJuridica);
        return repository.save(document)
                .map(pj -> pj.toDomain());
    }

    @Override
    public Mono<Boolean> existsByCnpj(String cnpj) {
        return repository.existsByCnpj(cnpj);
    }
}
