package com.faker.receita.infrastructure.web.dto.pf;

public record SituacaoReceitaDto(
        String codigo,
        String descricao,
        String dataInscricao,
        String dataConsulta
) {}
