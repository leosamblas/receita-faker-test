package com.faker.receita.domain.model.pf;

public record Endereco(
        String cep,
        String cepFormatado,
        String tipoLogradouro,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String municipio,
        String codigoMunicipioIbge,
        String codigoMunicipioTom,
        String uf,
        String pais
) {}
