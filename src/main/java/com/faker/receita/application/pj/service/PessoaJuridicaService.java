package com.faker.receita.application.pj.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import com.faker.receita.application.common.data.AddressGenerator;
import com.faker.receita.application.common.data.IbgeTomDatabase;
import com.faker.receita.application.common.util.TextSanitizer;
import com.faker.receita.application.pj.port.in.PessoaJuridicaUseCase;
import com.faker.receita.application.pj.port.out.PessoaJuridicaRepositoryPort;
import com.faker.receita.domain.exception.InvalidDocumentException;
import com.faker.receita.domain.model.pj.CnaeSecundario;
import com.faker.receita.domain.model.pj.PessoaJuridica;
import com.faker.receita.domain.model.pj.RegimeTributario;
import com.faker.receita.domain.model.pj.SituacaoCadastralPj;
import com.faker.receita.domain.model.pj.Socio;
import com.faker.receita.domain.validation.CnpjValidator;
import com.faker.receita.domain.validation.CpfValidator;

import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PessoaJuridicaService implements PessoaJuridicaUseCase {

    private record CnaeInfo(int codigo, String descricao) {}

    private static final List<CnaeInfo> CNAE_CATALOG = List.of(
            new CnaeInfo(8299702, "Emissão de vales-alimentação, vales-transporte e similares"),
            new CnaeInfo(6619302, "Correspondentes de instituições financeiras"),
            new CnaeInfo(6201501, "Desenvolvimento de programas de computador sob encomenda"),
            new CnaeInfo(6202300, "Desenvolvimento e licenciamento de programas de computador customizáveis"),
            new CnaeInfo(6204000, "Consultoria em tecnologia da informação"),
            new CnaeInfo(7020400, "Atividades de consultoria em gestão empresarial"),
            new CnaeInfo(4711302, "Comércio varejista de mercadorias em geral, com predominância de produtos alimentícios"),
            new CnaeInfo(4751201, "Comércio varejista de equipamentos e suprimentos de informática"),
            new CnaeInfo(4930202, "Transporte rodoviário de carga"),
            new CnaeInfo(5611201, "Restaurantes e similares"),
            new CnaeInfo(6499999, "Outras atividades de serviços financeiros não especificadas anteriormente"),
            new CnaeInfo(6311900, "Tratamento de dados, provedores de serviços de aplicação e hospedagem na internet")
    );

    private static final List<String> QUALIFICACOES_SOCIO = List.of(
            "Sócio-Administrador",
            "Conselheiro de Administração",
            "Diretor",
            "Presidente",
            "Sócio"
    );

    private static final List<String> FAIXAS_ETARIAS = List.of(
            "Entre 21 a 30 anos",
            "Entre 31 a 40 anos",
            "Entre 41 a 50 anos",
            "Entre 51 a 60 anos",
            "Entre 61 a 70 anos",
            "Mais de 70 anos"
    );

    private final Faker faker;
    private final CnpjValidator cnpjValidator;
    private final CpfValidator cpfValidator;
    private final IbgeTomDatabase ibgeTomDatabase;
    private final AddressGenerator addressGenerator;
    private final PessoaJuridicaRepositoryPort pessoaJuridicaRepositoryPort;
    private final Clock clock;

    @Override
    public Mono<PessoaJuridica> generateRandom() {
        return Mono.defer(() -> {
            String generatedCnpj = cnpjValidator.generateValidCnpj();
            return getOrCreateByCnpj(generatedCnpj);
        });
    }

    @Override
    public Mono<PessoaJuridica> getOrCreateByCnpj(String rawOrFormattedCnpj) {
        return Mono.defer(() -> {
            String cleanCnpj;
            try {
                cleanCnpj = cnpjValidator.cleanAndValidate(rawOrFormattedCnpj);
            } catch (InvalidDocumentException e) {
                return Mono.error(e);
            }

            return pessoaJuridicaRepositoryPort.findByCnpj(cleanCnpj)
                    .switchIfEmpty(Mono.defer(() -> {
                        PessoaJuridica generated = buildPessoaJuridica(cleanCnpj);
                        return pessoaJuridicaRepositoryPort.save(generated);
                    }));
        });
    }

    private PessoaJuridica buildPessoaJuridica(String cleanCnpj) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // 1. Razão Social e Nome Fantasia
        String baseName = TextSanitizer.sanitizeToUpper(faker.company().name().replaceAll("[.,]", ""));
        String suffix = pickLegalSuffix(random);
        String razaoSocial = baseName + " " + suffix;
        String nomeFantasia = baseName.split(" ")[0];

        // 2. Endereço e Município
        IbgeTomDatabase.CidadeInfo cidade = ibgeTomDatabase.getRandomCidade();
        String uf = cidade.uf();
        String cep = cidade.cepBase() + String.format("%03d", random.nextInt(10, 990));
        String bairro = ibgeTomDatabase.getRandomBairro();
        String logradouro = TextSanitizer.sanitizeToUpper(faker.address().streetName());
        String numero = addressGenerator.generateNumero(random);
        String complemento = addressGenerator.generateComplemento(random);

        String municipio = cidade.municipio();
        Integer codigoMunicipio = Integer.parseInt(cidade.codigoMunicipioTom());
        Integer codigoMunicipioIbge = Integer.parseInt(cidade.codigoMunicipioIbge());

        // 3. Porte, Natureza Jurídica e Capital Social
        String[] portes = {"ME", "EPP", "DEMAIS"};
        String porte = portes[random.nextInt(portes.length)];

        String naturezaJuridica = pickNaturezaJuridica(suffix, random);
        Double capitalSocial = calculateCapitalSocial(porte, random);

        // 4. CNAE Fiscal Principal e Secundários
        List<CnaeInfo> shuffledCnaes = new ArrayList<>(CNAE_CATALOG);
        Collections.shuffle(shuffledCnaes);
        CnaeInfo principalCnae = shuffledCnaes.get(0);
        Integer cnaeFiscal = principalCnae.codigo();
        String cnaeFiscalDescricao = principalCnae.descricao();

        int secondaryCount = random.nextInt(1, 3);
        List<CnaeSecundario> cnaesSecundarios = new ArrayList<>();
        for (int i = 1; i <= secondaryCount && i < shuffledCnaes.size(); i++) {
            CnaeInfo sec = shuffledCnaes.get(i);
            cnaesSecundarios.add(new CnaeSecundario(sec.codigo(), sec.descricao()));
        }

        // 5. Situação Cadastral
        String descricaoSituacaoCadastral = pickSituacaoCadastral(random).getDescricao();

        // 6. QSA (Quadro de Sócios e Administradores)
        int sociosCount = random.nextInt(1, 3);
        List<Socio> qsa = new ArrayList<>();
        for (int i = 0; i < sociosCount; i++) {
            boolean isMale = random.nextBoolean();
            String fName = isMale ? faker.name().maleFirstName() : faker.name().femaleFirstName();
            String lName1 = faker.name().lastName();
            String lName2 = faker.name().lastName();
            String nomeSocio = TextSanitizer.sanitizeToUpper(fName + " " + lName1 + " " + lName2);
            String qualificacao = QUALIFICACOES_SOCIO.get(random.nextInt(QUALIFICACOES_SOCIO.size()));
            String faixaEtaria = FAIXAS_ETARIAS.get(random.nextInt(FAIXAS_ETARIAS.size()));
            String cpf = cpfValidator.generateValidCpf();
            qsa.add(new Socio(cpf, nomeSocio, qualificacao, faixaEtaria));
        }

        // 7. Regime Tributário
        int anoAtual = LocalDate.now(clock).getYear();
        String formaDeTributacao;
        if ("ME".equals(porte) || "EPP".equals(porte)) {
            formaDeTributacao = random.nextBoolean() ? "SIMPLES NACIONAL" : "LUCRO PRESUMIDO";
        } else {
            formaDeTributacao = random.nextBoolean() ? "LUCRO REAL" : "LUCRO PRESUMIDO";
        }

        List<RegimeTributario> regimeTributario = List.of(
                new RegimeTributario(anoAtual, formaDeTributacao, 1)
        );

        return new PessoaJuridica(
                cleanCnpj,
                razaoSocial,
                nomeFantasia,
                uf,
                cep,
                bairro,
                logradouro,
                numero,
                complemento,
                municipio,
                codigoMunicipio,
                codigoMunicipioIbge,
                porte,
                capitalSocial,
                naturezaJuridica,
                cnaeFiscal,
                cnaeFiscalDescricao,
                descricaoSituacaoCadastral,
                qsa,
                cnaesSecundarios,
                regimeTributario
        );
    }

    private String pickLegalSuffix(ThreadLocalRandom random) {
        String[] suffixes = {"LTDA", "S.A.", "ME", "EIRELI"};
        return suffixes[random.nextInt(suffixes.length)];
    }

    private String pickNaturezaJuridica(String suffix, ThreadLocalRandom random) {
        if ("S.A.".equals(suffix)) {
            return random.nextBoolean() ? "Sociedade Anônima Fechada" : "Sociedade Anônima Aberta";
        }
        if ("EIRELI".equals(suffix)) {
            return "Empresa Individual de Responsabilidade Limitada";
        }
        if ("ME".equals(suffix)) {
            return "Empresário Individual";
        }
        return "Sociedade Empresária Limitada";
    }

    private Double calculateCapitalSocial(String porte, ThreadLocalRandom random) {
        if ("ME".equals(porte)) {
            return Math.round(random.nextDouble(10000.0, 100000.0) * 100.0) / 100.0;
        } else if ("EPP".equals(porte)) {
            return Math.round(random.nextDouble(100000.0, 1000000.0) * 100.0) / 100.0;
        } else {
            return Math.round(random.nextDouble(1000000.0, 500000000.0) * 100.0) / 100.0;
        }
    }

    SituacaoCadastralPj pickSituacaoCadastral(ThreadLocalRandom random) {
        int roll = random.nextInt(100);
        if (roll < 90) {
            return SituacaoCadastralPj.ATIVA;
        } else if (roll < 95) {
            return SituacaoCadastralPj.BAIXADA;
        } else if (roll < 98) {
            return SituacaoCadastralPj.INAPTA;
        } else if (roll < 99) {
            return SituacaoCadastralPj.SUSPENSA;
        } else {
            return SituacaoCadastralPj.NULA;
        }
    }
}
