package com.faker.receita.application.pf.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import java.time.LocalDate;
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
import com.faker.receita.application.pf.port.out.PessoaFisicaRepositoryPort;
import com.faker.receita.domain.exception.InvalidDocumentException;
import com.faker.receita.domain.model.pf.Contato;
import com.faker.receita.domain.model.pf.DadosCadastrais;
import com.faker.receita.domain.model.pf.Endereco;
import com.faker.receita.domain.model.pf.PessoaFisica;
import com.faker.receita.domain.model.pf.SituacaoCadastralPf;
import com.faker.receita.domain.model.pf.SituacaoReceita;
import com.faker.receita.domain.validation.CpfValidator;

import net.datafaker.Faker;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class PessoaFisicaServiceTest {

    private PessoaFisicaService service;
    private CpfValidator cpfValidator;
    private Clock fixedClock;

    @Mock
    private PessoaFisicaRepositoryPort pessoaFisicaRepositoryPort;

    @BeforeEach
    void setUp() {
        Faker faker = new Faker(Locale.of("pt", "BR"));
        cpfValidator = new CpfValidator();
        IbgeTomDatabase ibgeTomDatabase = new IbgeTomDatabase();
        AddressGenerator addressGenerator = new AddressGenerator();
        fixedClock = Clock.fixed(Instant.parse("2025-06-15T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));

        service = new PessoaFisicaService(
                faker,
                cpfValidator,
                ibgeTomDatabase,
                addressGenerator,
                pessoaFisicaRepositoryPort,
                fixedClock
        );
    }

    @Test
    @DisplayName("Deve gerar Pessoa Física aleatória e salvar através da porta de repositório quando não existir")
    void shouldGenerateRandomPessoaFisicaAndSave() {
        when(pessoaFisicaRepositoryPort.findByNumeroCpf(any())).thenReturn(Mono.empty());
        when(pessoaFisicaRepositoryPort.save(any(PessoaFisica.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.generateRandom())
                .assertNext(response -> {
                    assertNotNull(response);
                    assertNotNull(response.dadosCadastrais());
                    String cpf = response.dadosCadastrais().numeroCpf();
                    assertEquals(11, cpf.length());
                    assertTrue(cpfValidator.isValid(cpf));

                    List<String> codigosValidos = List.of("0", "2", "3", "4", "5", "8");
                    List<String> descricoesValidas = List.of(
                            "REGULAR", "SUSPENSA", "TITULAR FALECIDO",
                            "PENDENTE DE REGULARIZAÇÃO", "CANCELADA POR MULTIPLICIDADE", "NULA"
                    );
                    assertTrue(codigosValidos.contains(response.situacaoReceita().codigo()));
                    assertTrue(descricoesValidas.contains(response.situacaoReceita().descricao()));

                    if ("3".equals(response.situacaoReceita().codigo())) {
                        assertEquals("TITULAR FALECIDO", response.situacaoReceita().descricao());
                        assertNotNull(response.dadosCadastrais().dataObito());
                        LocalDate dataObito = LocalDate.parse(response.dadosCadastrais().dataObito());
                        LocalDate dataInscricao = LocalDate.parse(response.situacaoReceita().dataInscricao());
                        LocalDate dataNascimento = LocalDate.parse(response.dadosCadastrais().dataNascimento());
                        assertTrue(dataObito.isAfter(dataNascimento));
                        assertFalse(dataObito.isBefore(dataInscricao));
                        assertFalse(dataObito.isAfter(LocalDate.now(fixedClock)));
                    } else {
                        assertNull(response.dadosCadastrais().dataObito());
                    }

                    assertFalse(response.contato().telefones().isEmpty());
                    assertFalse(response.contato().emails().isEmpty());
                })
                .verifyComplete();

        verify(pessoaFisicaRepositoryPort, times(1)).save(any(PessoaFisica.class));
    }

    @Test
    @DisplayName("Deve gerar data de óbito minimamente válida situada entre inscrição e hoje")
    void shouldGenerateValidDeathDateBetweenInscriptionAndToday() {
        LocalDate today = LocalDate.now(fixedClock);
        LocalDate dataInscricao = today.minusYears(10);
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < 100; i++) {
            String dataObitoStr = service.generateDataObito(dataInscricao, today, random);
            assertNotNull(dataObitoStr);
            LocalDate dataObito = LocalDate.parse(dataObitoStr);
            assertFalse(dataObito.isBefore(dataInscricao));
            assertFalse(dataObito.isAfter(today));
        }
    }

    @Test
    @DisplayName("Deve gerar data de óbito igual a hoje quando dataInscricao for hoje")
    void shouldGenerateValidDeathDateWhenInscriptionIsToday() {
        LocalDate today = LocalDate.now(fixedClock);
        ThreadLocalRandom random = ThreadLocalRandom.current();

        String dataObitoStr = service.generateDataObito(today, today, random);
        assertEquals(today.toString(), dataObitoStr);
    }

    @Test
    @DisplayName("Deve mapear corretamente os valores e códigos da SituacaoCadastralPf")
    void shouldProperlyMapSituacaoCadastralPfValues() {
        assertEquals("0", SituacaoCadastralPf.REGULAR.getCodigo());
        assertEquals("REGULAR", SituacaoCadastralPf.REGULAR.getDescricao());
        assertFalse(SituacaoCadastralPf.REGULAR.isFalecido());

        assertEquals("2", SituacaoCadastralPf.SUSPENSA.getCodigo());
        assertEquals("SUSPENSA", SituacaoCadastralPf.SUSPENSA.getDescricao());
        assertFalse(SituacaoCadastralPf.SUSPENSA.isFalecido());

        assertEquals("3", SituacaoCadastralPf.TITULAR_FALECIDO.getCodigo());
        assertEquals("TITULAR FALECIDO", SituacaoCadastralPf.TITULAR_FALECIDO.getDescricao());
        assertTrue(SituacaoCadastralPf.TITULAR_FALECIDO.isFalecido());

        assertEquals("4", SituacaoCadastralPf.PENDENTE_DE_REGULARIZACAO.getCodigo());
        assertEquals("PENDENTE DE REGULARIZAÇÃO", SituacaoCadastralPf.PENDENTE_DE_REGULARIZACAO.getDescricao());
        assertFalse(SituacaoCadastralPf.PENDENTE_DE_REGULARIZACAO.isFalecido());

        assertEquals("5", SituacaoCadastralPf.CANCELADA_POR_MULTIPLICIDADE.getCodigo());
        assertEquals("CANCELADA POR MULTIPLICIDADE", SituacaoCadastralPf.CANCELADA_POR_MULTIPLICIDADE.getDescricao());
        assertFalse(SituacaoCadastralPf.CANCELADA_POR_MULTIPLICIDADE.isFalecido());

        assertEquals("8", SituacaoCadastralPf.NULA.getCodigo());
        assertEquals("NULA", SituacaoCadastralPf.NULA.getDescricao());
        assertFalse(SituacaoCadastralPf.NULA.isFalecido());
    }

    @Test
    @DisplayName("Deve sortear os status da receita cobrindo todos os status e priorizando REGULAR")
    void shouldCoverAllStatusesInRandomizationWithRegularAsMajority() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Set<SituacaoCadastralPf> generated = EnumSet.noneOf(SituacaoCadastralPf.class);
        int regularCount = 0;
        int total = 2000;

        for (int i = 0; i < total; i++) {
            SituacaoCadastralPf status = service.pickSituacaoCadastral(random);
            generated.add(status);
            if (status == SituacaoCadastralPf.REGULAR) {
                regularCount++;
            }
        }

        // REGULAR deve ser a vasta maioria (~90%)
        assertTrue(regularCount > total * 0.80, "REGULAR deveria representar a grande maioria dos sorteios");
        // Todos os status devem ser sorteados com 2000 amostras
        assertEquals(SituacaoCadastralPf.values().length, generated.size(), "Todos os status cadastrais deveriam ser alcançáveis");
    }

    @Test
    @DisplayName("Deve buscar registro existente através da porta quando CPF já estiver cadastrado")
    void shouldReturnExistingRecordWhenCpfAlreadyExists() {
        String validCpf = cpfValidator.generateValidCpf();
        DadosCadastrais dadosExistentes = new DadosCadastrais(
                validCpf,
                cpfValidator.format(validCpf),
                validCpf.substring(9, 11),
                "CLIENTE EXISTENTE",
                null,
                "1990-01-01",
                "M",
                "BRASILEIRA",
                "BRASIL",
                "MAE EXISTENTE",
                "123456789012",
                null);
        PessoaFisica pfSalva = new PessoaFisica(
                dadosExistentes,
                new SituacaoReceita("0", "REGULAR", "2010-01-01", "2010-01-01"),
                new Endereco("01001000", "01001-000", "RUA", "FLORES", "10", null, "CENTRO", "SAO PAULO", "3550308", "7107", "SP", "BRASIL"),
                new Contato(List.of(), List.of())
        );

        when(pessoaFisicaRepositoryPort.findByNumeroCpf(eq(validCpf))).thenReturn(Mono.just(pfSalva));

        StepVerifier.create(service.getOrCreateByCpf(validCpf))
                .assertNext(response -> {
                    assertEquals(validCpf, response.dadosCadastrais().numeroCpf());
                    assertEquals("CLIENTE EXISTENTE", response.dadosCadastrais().nomePessoaFisica());
                })
                .verifyComplete();

        verify(pessoaFisicaRepositoryPort, never()).save(any(PessoaFisica.class));
    }

    @Test
    @DisplayName("Deve falhar com InvalidDocumentException sem consultar repositório quando CPF for inválido")
    void shouldFailWhenGeneratingWithInvalidCpf() {
        StepVerifier.create(service.getOrCreateByCpf("111.111.111-11"))
                .expectError(InvalidDocumentException.class)
                .verify();

        verifyNoInteractions(pessoaFisicaRepositoryPort);
    }
}
