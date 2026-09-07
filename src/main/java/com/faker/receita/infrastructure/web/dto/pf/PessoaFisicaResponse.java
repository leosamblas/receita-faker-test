package com.faker.receita.infrastructure.web.dto.pf;

import com.faker.receita.domain.model.pf.PessoaFisica;

public record PessoaFisicaResponse(
        DadosCadastraisDto dadosCadastrais,
        SituacaoReceitaDto situacaoReceita,
        EnderecoDto endereco,
        ContatoDto contato
) {
    public static PessoaFisicaResponse fromDomain(PessoaFisica domain) {
        DadosCadastraisDto dadosDto = new DadosCadastraisDto(
                domain.dadosCadastrais().numeroCpf(),
                domain.dadosCadastrais().cpfFormatado(),
                domain.dadosCadastrais().digitoVerificador(),
                domain.dadosCadastrais().nomePessoaFisica(),
                domain.dadosCadastrais().nomeSocial(),
                domain.dadosCadastrais().dataNascimento(),
                domain.dadosCadastrais().sexo(),
                domain.dadosCadastrais().nacionalidade(),
                domain.dadosCadastrais().paisNascimento(),
                domain.dadosCadastrais().nomeMae(),
                domain.dadosCadastrais().tituloEleitor(),
                domain.dadosCadastrais().dataObito()
        );

        SituacaoReceitaDto situacaoDto = new SituacaoReceitaDto(
                domain.situacaoReceita().codigo(),
                domain.situacaoReceita().descricao(),
                domain.situacaoReceita().dataInscricao(),
                domain.situacaoReceita().dataConsulta()
        );

        EnderecoDto enderecoDto = new EnderecoDto(
                domain.endereco().cep(),
                domain.endereco().cepFormatado(),
                domain.endereco().tipoLogradouro(),
                domain.endereco().logradouro(),
                domain.endereco().numero(),
                domain.endereco().complemento(),
                domain.endereco().bairro(),
                domain.endereco().municipio(),
                domain.endereco().codigoMunicipioIbge(),
                domain.endereco().codigoMunicipioTom(),
                domain.endereco().uf(),
                domain.endereco().pais()
        );

        ContatoDto contatoDto = new ContatoDto(
                domain.contato().telefones().stream()
                        .map(t -> new TelefoneDto(t.tipo(), t.ddd(), t.numero()))
                        .toList(),
                domain.contato().emails().stream()
                        .map(e -> new EmailDto(e.tipo(), e.email()))
                        .toList()
        );

        return new PessoaFisicaResponse(dadosDto, situacaoDto, enderecoDto, contatoDto);
    }
}
