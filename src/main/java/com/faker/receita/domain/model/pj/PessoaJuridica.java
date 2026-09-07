package com.faker.receita.domain.model.pj;

import java.util.List;

public record PessoaJuridica(
        String cnpj,
        String razaoSocial,
        String nomeFantasia,
        String uf,
        String cep,
        String bairro,
        String logradouro,
        String numero,
        String complemento,
        String municipio,
        Integer codigoMunicipio,
        Integer codigoMunicipioIbge,
        String porte,
        Double capitalSocial,
        String naturezaJuridica,
        Integer cnaeFiscal,
        String cnaeFiscalDescricao,
        String descricaoSituacaoCadastral,
        List<Socio> qsa,
        List<CnaeSecundario> cnaesSecundarios,
        List<RegimeTributario> regimeTributario
) {}
