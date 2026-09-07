package com.faker.receita.application.pf.service;

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
import com.faker.receita.application.pf.port.out.PessoaFisicaRepositoryPort;
import com.faker.receita.application.pf.service.PessoaFisicaService;
import com.faker.receita.domain.exception.InvalidDocumentException;
import com.faker.receita.domain.model.pf.Contato;
import com.faker.receita.domain.model.pf.DadosCadastrais;
import com.faker.receita.domain.model.pf.Endereco;
import com.faker.receita.domain.model.pf.PessoaFisica;
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
                    assertEquals("0", response.situacaoReceita().codigo());
                    assertEquals("REGULAR", response.situacaoReceita().descricao());
                    assertFalse(response.contato().telefones().isEmpty());
                    assertFalse(response.contato().emails().isEmpty());
                })
                .verifyComplete();

        verify(pessoaFisicaRepositoryPort, times(1)).save(any(PessoaFisica.class));
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
