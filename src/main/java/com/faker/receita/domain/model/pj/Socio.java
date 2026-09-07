package com.faker.receita.domain.model.pj;

public record Socio(
        String cpf,
        String nomeSocio,
        String qualificacaoSocio,
        String faixaEtaria
) {
    public Socio(String nomeSocio, String qualificacaoSocio, String faixaEtaria) {
        this(null, nomeSocio, qualificacaoSocio, faixaEtaria);
    }
}
