package com.faker.receita.infrastructure.persistence.mongodb.document;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.faker.receita.domain.model.pj.CnaeSecundario;
import com.faker.receita.domain.model.pj.PessoaJuridica;
import com.faker.receita.domain.model.pj.RegimeTributario;
import com.faker.receita.domain.model.pj.Socio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "pessoas_juridicas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PessoaJuridicaDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String cnpj;

    private String razaoSocial;
    private String nomeFantasia;
    private String uf;
    private String cep;
    private String bairro;
    private String logradouro;
    private String numero;
    private String complemento;
    private String municipio;
    private Integer codigoMunicipio;
    private Integer codigoMunicipioIbge;
    private String porte;
    private Double capitalSocial;
    private String naturezaJuridica;
    private Integer cnaeFiscal;
    private String cnaeFiscalDescricao;
    private String descricaoSituacaoCadastral;
    private List<Socio> qsa;
    private List<CnaeSecundario> cnaesSecundarios;
    private List<RegimeTributario> regimeTributario;

    @CreatedDate
    private LocalDateTime dataCriacao;

    public PessoaJuridica toDomain() {
        return new PessoaJuridica(
                this.cnpj,
                this.razaoSocial,
                this.nomeFantasia,
                this.uf,
                this.cep,
                this.bairro,
                this.logradouro,
                this.numero,
                this.complemento,
                this.municipio,
                this.codigoMunicipio,
                this.codigoMunicipioIbge,
                this.porte,
                this.capitalSocial,
                this.naturezaJuridica,
                this.cnaeFiscal,
                this.cnaeFiscalDescricao,
                this.descricaoSituacaoCadastral,
                this.qsa,
                this.cnaesSecundarios,
                this.regimeTributario);
    }

    public static PessoaJuridicaDocument fromDomain(PessoaJuridica domain) {
        return PessoaJuridicaDocument.builder()
                .cnpj(domain.cnpj())
                .razaoSocial(domain.razaoSocial())
                .nomeFantasia(domain.nomeFantasia())
                .uf(domain.uf())
                .cep(domain.cep())
                .bairro(domain.bairro())
                .logradouro(domain.logradouro())
                .numero(domain.numero())
                .complemento(domain.complemento())
                .municipio(domain.municipio())
                .codigoMunicipio(domain.codigoMunicipio())
                .codigoMunicipioIbge(domain.codigoMunicipioIbge())
                .porte(domain.porte())
                .capitalSocial(domain.capitalSocial())
                .naturezaJuridica(domain.naturezaJuridica())
                .cnaeFiscal(domain.cnaeFiscal())
                .cnaeFiscalDescricao(domain.cnaeFiscalDescricao())
                .descricaoSituacaoCadastral(domain.descricaoSituacaoCadastral())
                .qsa(domain.qsa())
                .cnaesSecundarios(domain.cnaesSecundarios())
                .regimeTributario(domain.regimeTributario())
                .build();
    }
}
