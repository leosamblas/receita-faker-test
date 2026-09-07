package com.faker.receita.infrastructure.web.dto.pj;

public record RegimeTributarioDto(
        Integer ano,
        String formaDeTributacao,
        Integer quantidadeDeEscrituracoes
) {}
