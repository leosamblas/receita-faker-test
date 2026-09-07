package com.faker.receita.infrastructure.web.dto.pj;

import java.util.List;

import com.faker.receita.domain.model.pj.PessoaJuridica;

public record PessoaJuridicaResponse(
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
        List<SocioDto> qsa,
        List<CnaeSecundarioDto> cnaesSecundarios,
        List<RegimeTributarioDto> regimeTributario
) {
    public static PessoaJuridicaResponse fromDomain(PessoaJuridica domain) {
        List<SocioDto> qsaDtos = domain.qsa().stream()
                .map(s -> new SocioDto(s.nomeSocio(), s.qualificacaoSocio(), s.faixaEtaria()))
                .toList();

        List<CnaeSecundarioDto> cnaeDtos = domain.cnaesSecundarios().stream()
                .map(c -> new CnaeSecundarioDto(c.codigo(), c.descricao()))
                .toList();

        List<RegimeTributarioDto> regimeDtos = domain.regimeTributario().stream()
                .map(r -> new RegimeTributarioDto(r.ano(), r.formaDeTributacao(), r.quantidadeDeEscrituracoes()))
                .toList();

        return new PessoaJuridicaResponse(
                domain.cnpj(),
                domain.razaoSocial(),
                domain.nomeFantasia(),
                domain.uf(),
                domain.cep(),
                domain.bairro(),
                domain.logradouro(),
                domain.numero(),
                domain.complemento(),
                domain.municipio(),
                domain.codigoMunicipio(),
                domain.codigoMunicipioIbge(),
                domain.porte(),
                domain.capitalSocial(),
                domain.naturezaJuridica(),
                domain.cnaeFiscal(),
                domain.cnaeFiscalDescricao(),
                domain.descricaoSituacaoCadastral(),
                qsaDtos,
                cnaeDtos,
                regimeDtos
        );
    }
}
