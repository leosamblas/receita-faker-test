package com.faker.receita.domain.model.pf;

public enum SituacaoCadastralPf {
    REGULAR("0", "REGULAR"),
    SUSPENSA("2", "SUSPENSA"),
    TITULAR_FALECIDO("3", "TITULAR FALECIDO"),
    PENDENTE_DE_REGULARIZACAO("4", "PENDENTE DE REGULARIZAÇÃO"),
    CANCELADA_POR_MULTIPLICIDADE("5", "CANCELADA POR MULTIPLICIDADE"),
    NULA("8", "NULA");

    private final String codigo;
    private final String descricao;

    SituacaoCadastralPf(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isFalecido() {
        return this == TITULAR_FALECIDO;
    }
}
