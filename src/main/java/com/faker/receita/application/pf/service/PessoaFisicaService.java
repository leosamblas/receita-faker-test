package com.faker.receita.application.pf.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import com.faker.receita.application.common.data.AddressGenerator;
import com.faker.receita.application.common.data.IbgeTomDatabase;
import com.faker.receita.application.common.util.TextSanitizer;
import com.faker.receita.application.pf.port.in.PessoaFisicaUseCase;
import com.faker.receita.application.pf.port.out.PessoaFisicaRepositoryPort;
import com.faker.receita.domain.exception.InvalidDocumentException;
import com.faker.receita.domain.model.pf.Contato;
import com.faker.receita.domain.model.pf.DadosCadastrais;
import com.faker.receita.domain.model.pf.Email;
import com.faker.receita.domain.model.pf.Endereco;
import com.faker.receita.domain.model.pf.PessoaFisica;
import com.faker.receita.domain.model.pf.SituacaoReceita;
import com.faker.receita.domain.model.pf.Telefone;
import com.faker.receita.domain.validation.CpfValidator;

import net.datafaker.Faker;
import reactor.core.publisher.Mono;

@Service
public class PessoaFisicaService implements PessoaFisicaUseCase {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Faker faker;
    private final CpfValidator cpfValidator;
    private final IbgeTomDatabase ibgeTomDatabase;
    private final AddressGenerator addressGenerator;
    private final PessoaFisicaRepositoryPort pessoaFisicaRepositoryPort;
    private final Clock clock;

    @org.springframework.beans.factory.annotation.Autowired
    public PessoaFisicaService(
            Faker faker,
            CpfValidator cpfValidator,
            IbgeTomDatabase ibgeTomDatabase,
            AddressGenerator addressGenerator,
            PessoaFisicaRepositoryPort pessoaFisicaRepositoryPort,
            Clock clock) {
        this.faker = faker;
        this.cpfValidator = cpfValidator;
        this.ibgeTomDatabase = ibgeTomDatabase;
        this.addressGenerator = addressGenerator;
        this.pessoaFisicaRepositoryPort = pessoaFisicaRepositoryPort;
        this.clock = clock;
    }

    public PessoaFisicaService(
            Faker faker,
            CpfValidator cpfValidator,
            IbgeTomDatabase ibgeTomDatabase,
            AddressGenerator addressGenerator,
            PessoaFisicaRepositoryPort pessoaFisicaRepositoryPort) {
        this(faker, cpfValidator, ibgeTomDatabase, addressGenerator, pessoaFisicaRepositoryPort, Clock.systemDefaultZone());
    }

    @Override
    public Mono<PessoaFisica> generateRandom() {
        return Mono.defer(() -> {
            String generatedCpf = cpfValidator.generateValidCpf();
            return getOrCreateByCpf(generatedCpf);
        });
    }

    @Override
    public Mono<PessoaFisica> getOrCreateByCpf(String rawOrFormattedCpf) {
        return Mono.defer(() -> {
            String cleanCpf;
            try {
                cleanCpf = cpfValidator.cleanAndValidate(rawOrFormattedCpf);
            } catch (InvalidDocumentException e) {
                return Mono.error(e);
            }

            return pessoaFisicaRepositoryPort.findByNumeroCpf(cleanCpf)
                    .switchIfEmpty(Mono.defer(() -> {
                        PessoaFisica generated = buildPessoaFisica(cleanCpf);
                        return pessoaFisicaRepositoryPort.save(generated);
                    }));
        });
    }

    private PessoaFisica buildPessoaFisica(String cleanCpf) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // 1. Dados Cadastrais
        String cpfFormatado = cpfValidator.format(cleanCpf);
        String digitoVerificador = cleanCpf.substring(9, 11);

        boolean isMale = random.nextBoolean();
        String sexo = isMale ? "M" : "F";

        String firstName = isMale ? faker.name().maleFirstName() : faker.name().femaleFirstName();
        String lastName1 = faker.name().lastName();
        String lastName2 = faker.name().lastName();
        String nomePessoaFisica = TextSanitizer.sanitizeToUpper(firstName + " " + lastName1 + " " + lastName2);

        String motherFirstName = faker.name().femaleFirstName();
        String motherLastName = faker.name().lastName();
        String nomeMae = TextSanitizer.sanitizeToUpper(motherFirstName + " " + lastName1 + " DE " + motherLastName);

        int age = random.nextInt(18, 75);
        int dayOfYear = random.nextInt(1, 365);
        LocalDate today = LocalDate.now(clock);
        LocalDate dataNascimento = today.minusYears(age).withDayOfYear(Math.min(dayOfYear, 365));

        String tituloEleitor = String.format("%012d", random.nextLong(100000000000L, 999999999999L));

        DadosCadastrais dadosCadastrais = new DadosCadastrais(
                cleanCpf,
                cpfFormatado,
                digitoVerificador,
                nomePessoaFisica,
                null,
                dataNascimento.format(ISO_DATE),
                sexo,
                "BRASILEIRA",
                "BRASIL",
                nomeMae,
                tituloEleitor,
                null);

        // 2. Situação Receita Federal
        LocalDate dataInscricao = dataNascimento.plusYears(random.nextInt(16, Math.min(22, age + 1)));
        if (dataInscricao.isAfter(today)) {
            dataInscricao = today.minusMonths(random.nextInt(1, 12));
        }

        SituacaoReceita situacaoReceita = new SituacaoReceita(
                "0",
                "REGULAR",
                dataInscricao.format(ISO_DATE),
                dataInscricao.format(ISO_DATE));

        // 3. Endereço e Município
        IbgeTomDatabase.CidadeInfo cidade = ibgeTomDatabase.getRandomCidade();
        String tipoLogradouro = ibgeTomDatabase.getRandomTipoLogradouro();
        String logradouro = TextSanitizer.sanitizeToUpper(faker.address().streetName());
        String numero = addressGenerator.generateNumero(random);
        String complemento = addressGenerator.generateComplemento(random);
        String bairro = ibgeTomDatabase.getRandomBairro();
        String cep = cidade.cepBase() + String.format("%03d", random.nextInt(10, 990));
        String cepFormatado = addressGenerator.formatCep(cep);

        Endereco endereco = new Endereco(
                cep,
                cepFormatado,
                tipoLogradouro,
                logradouro,
                numero,
                complemento,
                bairro,
                cidade.municipio(),
                cidade.codigoMunicipioIbge(),
                cidade.codigoMunicipioTom(),
                cidade.uf(),
                "BRASIL");

        // 4. Contato (Telefones e E-mails)
        String ddd = cidade.ddd();
        String celularNumero = "9" + String.format("%08d", random.nextInt(10000000, 99999999));
        String fixoNumero = String.format("%08d", random.nextInt(30000000, 39999999));

        List<Telefone> telefones = List.of(
                new Telefone("CELULAR", ddd, celularNumero),
                new Telefone("FIXO", ddd, fixoNumero));

        String cleanFirstName = TextSanitizer.cleanForEmail(firstName);
        String cleanLastName = TextSanitizer.cleanForEmail(lastName2);
        String emailPrincipal = cleanFirstName + "." + cleanLastName + "@email.com";
        String emailAlternativo = cleanFirstName + ".contato@empresa.com.br";

        List<Email> emails = List.of(
                new Email("PRINCIPAL", emailPrincipal),
                new Email("ALTERNATIVO", emailAlternativo));

        Contato contato = new Contato(telefones, emails);

        return new PessoaFisica(dadosCadastrais, situacaoReceita, endereco, contato);
    }
}
