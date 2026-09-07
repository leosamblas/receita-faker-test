package com.faker.receita.domain.model.pj;

public record RegimeTributario(
        Integer ano,
        String formaDeTributacao,
        Integer quantidadeDeEscrituracoes
) {}
