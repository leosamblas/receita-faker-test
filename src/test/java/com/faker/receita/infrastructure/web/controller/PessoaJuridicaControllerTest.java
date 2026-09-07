package com.faker.receita.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.faker.receita.application.pj.port.in.PessoaJuridicaUseCase;
import com.faker.receita.domain.exception.InvalidDocumentException;
import com.faker.receita.domain.model.pj.CnaeSecundario;
import com.faker.receita.domain.model.pj.PessoaJuridica;
import com.faker.receita.domain.model.pj.RegimeTributario;
import com.faker.receita.domain.model.pj.Socio;
import com.faker.receita.infrastructure.web.dto.pj.PessoaJuridicaResponse;
import com.faker.receita.infrastructure.web.exception.GlobalExceptionHandler;

import reactor.core.publisher.Mono;

class PessoaJuridicaControllerTest {

    private WebTestClient webTestClient;
    private PessoaJuridicaUseCase pessoaJuridicaUseCase;

    private PessoaJuridica samplePessoaJuridica;

    @BeforeEach
    void setUp() {
        pessoaJuridicaUseCase = mock(PessoaJuridicaUseCase.class);

        samplePessoaJuridica = new PessoaJuridica(
                "04740876000125",
                "ALELO S.A.",
                "ALELO",
                "SP",
                "06455030",
                "ALPHAVILLE CENTRO INDUSTRIAL E EMPRESARIAL",
                "XINGU",
                "512",
                "ANDAR 3",
                "BARUERI",
                6213,
                3505708,
                "DEMAIS",
                472414100.0,
                "Sociedade Anônima Fechada",
                8299702,
                "Emissão de vales-alimentação, vales-transporte e similares",
                "ATIVA",
                List.of(new Socio("52998224725", "ANA JULIA DE VASCONCELOS CAREPA", "Conselheiro de Administração", "Entre 61 a 70 anos")),
                List.of(new CnaeSecundario(6619302, "Correspondentes de instituições financeiras")),
                List.of(new RegimeTributario(2024, "LUCRO REAL", 1))
        );

        PessoaJuridicaController controller = new PessoaJuridicaController(pessoaJuridicaUseCase);

        this.webTestClient = WebTestClient.bindToController(controller)
                .controllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/pj - Deve gerar Pessoa Jurídica aleatória com sucesso (Status 200)")
    void shouldGenerateRandomPessoaJuridicaSuccessfully() {
        when(pessoaJuridicaUseCase.generateRandom()).thenReturn(Mono.just(samplePessoaJuridica));

        webTestClient.get()
                .uri("/api/v1/pj")
                .exchange()
                .expectStatus().isOk()
                .expectBody(PessoaJuridicaResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("04740876000125", response.cnpj());
                    assertEquals("ALELO S.A.", response.razaoSocial());
                    assertEquals("ALELO", response.nomeFantasia());
                    assertEquals("BARUERI", response.municipio());
                    assertEquals(6213, response.codigoMunicipio());
                    assertEquals(3505708, response.codigoMunicipioIbge());
                    assertFalse(response.qsa().isEmpty());
                    assertEquals("52998224725", response.qsa().get(0).cpf());
                    assertFalse(response.cnaesSecundarios().isEmpty());
                    assertFalse(response.regimeTributario().isEmpty());
                });
    }

    @Test
    @DisplayName("GET /api/v1/pj/{cnpj} - Deve obter Pessoa Jurídica por CNPJ com sucesso (Status 200)")
    void shouldGetPessoaJuridicaByCnpjSuccessfully() {
        when(pessoaJuridicaUseCase.getOrCreateByCnpj("04740876000125")).thenReturn(Mono.just(samplePessoaJuridica));

        webTestClient.get()
                .uri("/api/v1/pj/04740876000125")
                .exchange()
                .expectStatus().isOk()
                .expectBody(PessoaJuridicaResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("04740876000125", response.cnpj());
                    assertEquals("ALELO S.A.", response.razaoSocial());
                });
    }

    @Test
    @DisplayName("GET /api/v1/pj/{cnpj} - Deve retornar 400 Bad Request ao informar CNPJ inválido")
    void shouldReturnBadRequestWhenCnpjIsInvalid() {
        when(pessoaJuridicaUseCase.getOrCreateByCnpj(anyString()))
                .thenReturn(Mono.error(new InvalidDocumentException("CNPJ inválido.")));

        webTestClient.get()
                .uri("/api/v1/pj/00000000000000")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request")
                .jsonPath("$.message").isEqualTo("CNPJ inválido.")
                .jsonPath("$.path").isEqualTo("/api/v1/pj/00000000000000");
    }
}
