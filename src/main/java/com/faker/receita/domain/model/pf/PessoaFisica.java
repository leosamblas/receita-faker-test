package com.faker.receita.domain.model.pf;

public record PessoaFisica(
        DadosCadastrais dadosCadastrais,
        SituacaoReceita situacaoReceita,
        Endereco endereco,
        Contato contato
) {}
