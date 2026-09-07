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
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.faker.receita.application.common.data.AddressGenerator;
import com.faker.receita.application.common.data.IbgeTomDatabase;
import com.faker.receita.application.pj.port.out.PessoaJuridicaRepositoryPort;
import com.faker.receita.domain.exception.InvalidDocumentException;
import com.faker.receita.domain.model.pj.CnaeSecundario;
import com.faker.receita.domain.model.pj.PessoaJuridica;
import com.faker.receita.domain.model.pj.RegimeTributario;
import com.faker.receita.domain.model.pj.SituacaoCadastralPj;
import com.faker.receita.domain.model.pj.Socio;
import com.faker.receita.domain.validation.CnpjValidator;
import com.faker.receita.domain.validation.CpfValidator;

import net.datafaker.Faker;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class PessoaJuridicaServiceTest {

    private PessoaJuridicaService service;
    private CnpjValidator cnpjValidator;
    private CpfValidator cpfValidator;
    private Clock fixedClock;

    @Mock
    private PessoaJuridicaRepositoryPort pessoaJuridicaRepositoryPort;

    @BeforeEach
    void setUp() {
        Faker faker = new Faker(Locale.of("pt", "BR"));
        cnpjValidator = new CnpjValidator();
        cpfValidator = new CpfValidator();
        IbgeTomDatabase ibgeTomDatabase = new IbgeTomDatabase();
        AddressGenerator addressGenerator = new AddressGenerator();
        fixedClock = Clock.fixed(Instant.parse("2024-01-01T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));

        service = new PessoaJuridicaService(
                faker,
                cnpjValidator,
                cpfValidator,
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
                    List<String> situacoesValidas = List.of("ATIVA", "SUSPENSA", "INAPTA", "BAIXADA", "NULA");
                    assertTrue(situacoesValidas.contains(response.descricaoSituacaoCadastral()));
                    assertFalse(response.qsa().isEmpty());
                    for (Socio socio : response.qsa()) {
                        assertNotNull(socio.cpf());
                        assertEquals(11, socio.cpf().length());
                        assertTrue(cpfValidator.isValid(socio.cpf()));
                    }
                    assertFalse(response.cnaesSecundarios().isEmpty());
                    assertFalse(response.regimeTributario().isEmpty());
                    assertEquals(2024, response.regimeTributario().get(0).ano());
                })
                .verifyComplete();

        verify(pessoaJuridicaRepositoryPort, times(1)).save(any(PessoaJuridica.class));
    }

    @Test
    @DisplayName("Deve mapear corretamente os valores e códigos da SituacaoCadastralPj")
    void shouldProperlyMapSituacaoCadastralPjValues() {
        assertEquals("02", SituacaoCadastralPj.ATIVA.getCodigo());
        assertEquals("ATIVA", SituacaoCadastralPj.ATIVA.getDescricao());

        assertEquals("01", SituacaoCadastralPj.SUSPENSA.getCodigo());
        assertEquals("SUSPENSA", SituacaoCadastralPj.SUSPENSA.getDescricao());

        assertEquals("04", SituacaoCadastralPj.INAPTA.getCodigo());
        assertEquals("INAPTA", SituacaoCadastralPj.INAPTA.getDescricao());

        assertEquals("08", SituacaoCadastralPj.BAIXADA.getCodigo());
        assertEquals("BAIXADA", SituacaoCadastralPj.BAIXADA.getDescricao());

        assertEquals("03", SituacaoCadastralPj.NULA.getCodigo());
        assertEquals("NULA", SituacaoCadastralPj.NULA.getDescricao());
    }

    @Test
    @DisplayName("Deve sortear os status da empresa cobrindo todos os status e priorizando ATIVA")
    void shouldCoverAllStatusesInRandomizationWithAtivaAsMajority() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Set<SituacaoCadastralPj> generated = EnumSet.noneOf(SituacaoCadastralPj.class);
        int ativaCount = 0;
        int total = 2000;

        for (int i = 0; i < total; i++) {
            SituacaoCadastralPj status = service.pickSituacaoCadastral(random);
            generated.add(status);
            if (status == SituacaoCadastralPj.ATIVA) {
                ativaCount++;
            }
        }

        // ATIVA deve ser a vasta maioria (~90%)
        assertTrue(ativaCount > total * 0.80, "ATIVA deveria representar a grande maioria dos sorteios");
        // Todos os status devem ser sorteados com 2000 amostras
        assertEquals(SituacaoCadastralPj.values().length, generated.size(), "Todos os status cadastrais deveriam ser alcançáveis");
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
                List.of(new Socio("52998224725", "ANA JULIA DE VASCONCELOS CAREPA", "Conselheiro de Administração", "Entre 61 a 70 anos")),
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
