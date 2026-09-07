package com.faker.receita.infrastructure.web.dto.pf;

public record EnderecoDto(
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
