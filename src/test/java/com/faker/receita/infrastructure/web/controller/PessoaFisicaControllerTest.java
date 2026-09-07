package com.faker.receita.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.faker.receita.application.pf.port.in.PessoaFisicaUseCase;
import com.faker.receita.domain.exception.InvalidDocumentException;
import com.faker.receita.domain.model.pf.Contato;
import com.faker.receita.domain.model.pf.DadosCadastrais;
import com.faker.receita.domain.model.pf.Email;
import com.faker.receita.domain.model.pf.Endereco;
import com.faker.receita.domain.model.pf.PessoaFisica;
import com.faker.receita.domain.model.pf.SituacaoReceita;
import com.faker.receita.domain.model.pf.Telefone;
import com.faker.receita.infrastructure.web.controller.PessoaFisicaController;
import com.faker.receita.infrastructure.web.dto.pf.PessoaFisicaResponse;
import com.faker.receita.infrastructure.web.exception.GlobalExceptionHandler;

import reactor.core.publisher.Mono;

class PessoaFisicaControllerTest {

    private WebTestClient webTestClient;
    private PessoaFisicaUseCase pessoaFisicaUseCase;

    private PessoaFisica samplePessoaFisica;

    @BeforeEach
    void setUp() {
        pessoaFisicaUseCase = Mockito.mock(PessoaFisicaUseCase.class);

        DadosCadastrais dados = new DadosCadastrais(
                "52998224725",
                "529.982.247-25",
                "25",
                "CARLOS SILVA",
                null,
                "1990-05-10",
                "M",
                "BRASILEIRA",
                "BRASIL",
                "MARIA SILVA",
                "123456789012",
                null);
        SituacaoReceita receita = new SituacaoReceita("0", "REGULAR", "2008-01-01", "2008-01-01");
        Endereco endereco = new Endereco("01001000", "01001-000", "RUA", "FLORES", "10", null, "CENTRO", "SAO PAULO", "3550308", "7107", "SP", "BRASIL");
        Contato contato = new Contato(List.of(new Telefone("CELULAR", "11", "987654321")), List.of(new Email("PRINCIPAL", "carlos@email.com")));

        samplePessoaFisica = new PessoaFisica(dados, receita, endereco, contato);

        PessoaFisicaController controller = new PessoaFisicaController(pessoaFisicaUseCase);

        this.webTestClient = WebTestClient.bindToController(controller)
                .controllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/pf - Deve gerar Pessoa Física aleatória com sucesso (Status 200)")
    void shouldGenerateRandomPessoaFisicaSuccessfully() {
        when(pessoaFisicaUseCase.generateRandom()).thenReturn(Mono.just(samplePessoaFisica));

        webTestClient.get()
                .uri("/api/v1/pf")
                .exchange()
                .expectStatus().isOk()
                .expectBody(PessoaFisicaResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertNotNull(response.dadosCadastrais());
                    assertEquals("52998224725", response.dadosCadastrais().numeroCpf());
                    assertEquals("529.982.247-25", response.dadosCadastrais().cpfFormatado());
                    assertEquals("0", response.situacaoReceita().codigo());
                    assertFalse(response.contato().telefones().isEmpty());
                });
    }

    @Test
    @DisplayName("GET /api/v1/pf/{cpf} - Deve obter Pessoa Física por CPF com sucesso (Status 200)")
    void shouldGetPessoaFisicaByCpfSuccessfully() {
        when(pessoaFisicaUseCase.getOrCreateByCpf("52998224725")).thenReturn(Mono.just(samplePessoaFisica));

        webTestClient.get()
                .uri("/api/v1/pf/52998224725")
                .exchange()
                .expectStatus().isOk()
                .expectBody(PessoaFisicaResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("52998224725", response.dadosCadastrais().numeroCpf());
                });
    }

    @Test
    @DisplayName("GET /api/v1/pf/{cpf} - Deve retornar 400 Bad Request para CPF inválido")
    void shouldReturnBadRequestWhenCpfIsInvalid() {
        when(pessoaFisicaUseCase.getOrCreateByCpf(anyString()))
                .thenReturn(Mono.error(new InvalidDocumentException("CPF inválido.")));

        webTestClient.get()
                .uri("/api/v1/pf/111.111.111-11")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request")
                .jsonPath("$.message").isEqualTo("CPF inválido.")
                .jsonPath("$.path").isEqualTo("/api/v1/pf/111.111.111-11");
    }
}
