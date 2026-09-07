package com.faker.receita.application.pj.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.faker.receita.application.common.data.AddressGenerator;
import com.faker.receita.application.common.data.IbgeTomDatabase;
import com.faker.receita.application.pj.port.out.PessoaJuridicaRepositoryPort;
import com.faker.receita.application.pj.service.PessoaJuridicaService;
import com.faker.receita.domain.exception.InvalidDocumentException;
import com.faker.receita.domain.model.pj.CnaeSecundario;
import com.faker.receita.domain.model.pj.PessoaJuridica;
import com.faker.receita.domain.model.pj.RegimeTributario;
import com.faker.receita.domain.model.pj.Socio;
import com.faker.receita.domain.validation.CnpjValidator;

import net.datafaker.Faker;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class PessoaJuridicaServiceTest {

    private PessoaJuridicaService service;
    private CnpjValidator cnpjValidator;
    private Clock fixedClock;

    @Mock
    private PessoaJuridicaRepositoryPort pessoaJuridicaRepositoryPort;

    @BeforeEach
    void setUp() {
        Faker faker = new Faker(Locale.of("pt", "BR"));
        cnpjValidator = new CnpjValidator();
        IbgeTomDatabase ibgeTomDatabase = new IbgeTomDatabase();
        AddressGenerator addressGenerator = new AddressGenerator();
        fixedClock = Clock.fixed(Instant.parse("2024-01-01T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));

        service = new PessoaJuridicaService(
                faker,
                cnpjValidator,
                ibgeTomDatabase,
                addressGenerator,
                pessoaJuridicaRepositoryPort,
                fixedClock
        );
    }

    @Test
    @DisplayName("Deve gerar Pessoa Jurídica aleatória e salvar através da porta de repositório quando não existir")
    void shouldGenerateRandomPessoaJuridicaAndSave() {
        when(pessoaJuridicaRepositoryPort.findByCnpj(any())).thenReturn(Mono.empty());
        when(pessoaJuridicaRepositoryPort.save(any(PessoaJuridica.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.generateRandom())
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(14, response.cnpj().length());
                    assertTrue(cnpjValidator.isValid(response.cnpj()));
                    assertNotNull(response.razaoSocial());
                    assertNotNull(response.nomeFantasia());
                    assertNotNull(response.uf());
                    assertNotNull(response.cep());
                    assertNotNull(response.municipio());
                    assertNotNull(response.codigoMunicipio());
                    assertNotNull(response.codigoMunicipioIbge());
                    assertNotNull(response.porte());
                    assertNotNull(response.capitalSocial());
                    assertNotNull(response.naturezaJuridica());
                    assertNotNull(response.cnaeFiscal());
                    assertNotNull(response.cnaeFiscalDescricao());
                    assertTrue(List.of("ATIVA", "SUSPENSA").contains(response.descricaoSituacaoCadastral()));
                    assertFalse(response.qsa().isEmpty());
                    assertFalse(response.cnaesSecundarios().isEmpty());
                    assertFalse(response.regimeTributario().isEmpty());
                    assertEquals(2024, response.regimeTributario().get(0).ano());
                })
                .verifyComplete();

        verify(pessoaJuridicaRepositoryPort, times(1)).save(any(PessoaJuridica.class));
    }

    @Test
    @DisplayName("Deve buscar registro existente através da porta quando CNPJ já estiver cadastrado")
    void shouldReturnExistingRecordWhenCnpjAlreadyExists() {
        String validCnpj = "04740876000125";
        PessoaJuridica pjSalva = new PessoaJuridica(
                validCnpj,
                "ALELO S.A.",
                "ALELO",
                "SP",
                "06455030",
                "ALPHAVILLE",
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
                List.of(new Socio("ANA JULIA DE VASCONCELOS CAREPA", "Conselheiro de Administração", "Entre 61 a 70 anos")),
                List.of(new CnaeSecundario(6619302, "Correspondentes de instituições financeiras")),
                List.of(new RegimeTributario(2024, "LUCRO REAL", 1))
        );

        when(pessoaJuridicaRepositoryPort.findByCnpj(eq(validCnpj))).thenReturn(Mono.just(pjSalva));

        StepVerifier.create(service.getOrCreateByCnpj(validCnpj))
                .assertNext(response -> {
                    assertEquals(validCnpj, response.cnpj());
                    assertEquals("ALELO S.A.", response.razaoSocial());
                    assertEquals("ALELO", response.nomeFantasia());
                    assertEquals("BARUERI", response.municipio());
                    assertEquals(6213, response.codigoMunicipio());
                    assertEquals(3505708, response.codigoMunicipioIbge());
                    assertEquals(472414100.0, response.capitalSocial());
                })
                .verifyComplete();

        verify(pessoaJuridicaRepositoryPort, never()).save(any(PessoaJuridica.class));
    }

    @Test
    @DisplayName("Deve falhar com InvalidDocumentException sem consultar repositório quando CNPJ for inválido")
    void shouldFailWhenGeneratingWithInvalidCnpj() {
        StepVerifier.create(service.getOrCreateByCnpj("00.000.000/0000-00"))
                .expectError(InvalidDocumentException.class)
                .verify();

        verifyNoInteractions(pessoaJuridicaRepositoryPort);
    }
}
