package com.faker.receita.domain.model.pj;

public enum SituacaoCadastralPj {
    ATIVA("02", "ATIVA"),
    SUSPENSA("01", "SUSPENSA"),
    INAPTA("04", "INAPTA"),
    BAIXADA("08", "BAIXADA"),
    NULA("03", "NULA");

    private final String codigo;
    private final String descricao;

    SituacaoCadastralPj(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }
}
