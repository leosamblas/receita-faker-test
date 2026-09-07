package com.faker.receita.domain.model.pf;

public record SituacaoReceita(
        String codigo,
        String descricao,
        String dataInscricao,
        String dataConsulta
) {}
