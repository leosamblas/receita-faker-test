package com.faker.receita.domain.model.pf;

public record DadosCadastrais(
        String numeroCpf,
        String cpfFormatado,
        String digitoVerificador,
        String nomePessoaFisica,
        String nomeSocial,
        String dataNascimento,
        String sexo,
        String nacionalidade,
        String paisNascimento,
        String nomeMae,
        String tituloEleitor,
        String dataObito
) {}
