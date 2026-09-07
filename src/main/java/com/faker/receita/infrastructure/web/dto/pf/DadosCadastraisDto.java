package com.faker.receita.infrastructure.web.dto.pf;

public record DadosCadastraisDto(
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
