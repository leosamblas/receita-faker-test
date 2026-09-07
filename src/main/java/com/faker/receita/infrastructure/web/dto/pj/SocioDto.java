package com.faker.receita.infrastructure.web.dto.pj;

public record SocioDto(
        String cpf,
        String nomeSocio,
        String qualificacaoSocio,
        String faixaEtaria
) {
    public SocioDto(String nomeSocio, String qualificacaoSocio, String faixaEtaria) {
        this(null, nomeSocio, qualificacaoSocio, faixaEtaria);
    }
}
