package com.faker.receita.infrastructure.persistence.mongodb.document;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.faker.receita.domain.model.pf.Contato;
import com.faker.receita.domain.model.pf.DadosCadastrais;
import com.faker.receita.domain.model.pf.Endereco;
import com.faker.receita.domain.model.pf.PessoaFisica;
import com.faker.receita.domain.model.pf.SituacaoReceita;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "pessoas_fisicas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PessoaFisicaDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String numeroCpf;

    private DadosCadastrais dadosCadastrais;
    private SituacaoReceita situacaoReceita;
    private Endereco endereco;
    private Contato contato;

    @CreatedDate
    private LocalDateTime dataCriacao;

    public PessoaFisica toDomain() {
        return new PessoaFisica(
                this.dadosCadastrais,
                this.situacaoReceita,
                this.endereco,
                this.contato);
    }

    public static PessoaFisicaDocument fromDomain(PessoaFisica domain) {
        return PessoaFisicaDocument.builder()
                .numeroCpf(domain.dadosCadastrais().numeroCpf())
                .dadosCadastrais(domain.dadosCadastrais())
                .situacaoReceita(domain.situacaoReceita())
                .endereco(domain.endereco())
                .contato(domain.contato())
                .build();
    }
}
