package com.faker.receita.application.common.data;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

@Component
public class IbgeTomDatabase {

    public record CidadeInfo(
            String municipio,
            String uf,
            String ddd,
            String codigoMunicipioIbge,
            String codigoMunicipioTom,
            String cepBase
    ) {}

    private static final List<CidadeInfo> CIDADES = List.of(
            new CidadeInfo("BARUERI", "SP", "11", "3505708", "6213", "06455"),
            new CidadeInfo("CAMPINAS", "SP", "19", "3509502", "6291", "13010"),
            new CidadeInfo("SAO PAULO", "SP", "11", "3550308", "7107", "01310"),
            new CidadeInfo("SANTOS", "SP", "13", "3548500", "7075", "11010"),
            new CidadeInfo("RIBEIRAO PRETO", "SP", "16", "3543402", "6973", "14010"),
            new CidadeInfo("SAO JOSE DOS CAMPOS", "SP", "12", "3549904", "7099", "12210"),
            new CidadeInfo("RIO DE JANEIRO", "RJ", "21", "3304557", "6001", "20040"),
            new CidadeInfo("NITEROI", "RJ", "21", "3303302", "5865", "24020"),
            new CidadeInfo("BELO HORIZONTE", "MG", "31", "3106200", "4123", "30130"),
            new CidadeInfo("UBERLANDIA", "MG", "34", "3170206", "5403", "38400"),
            new CidadeInfo("CURITIBA", "PR", "41", "4106902", "7535", "80010"),
            new CidadeInfo("LONDRINA", "PR", "43", "4113700", "7667", "86010"),
            new CidadeInfo("PORTO ALEGRE", "RS", "51", "4314902", "8599", "90010"),
            new CidadeInfo("CAXIAS DO SUL", "RS", "54", "4305108", "8531", "95010"),
            new CidadeInfo("FLORIANOPOLIS", "SC", "48", "4205407", "8105", "88010"),
            new CidadeInfo("JOINVILLE", "SC", "47", "4209102", "8179", "89201"),
            new CidadeInfo("SALVADOR", "BA", "71", "2927408", "3849", "40020"),
            new CidadeInfo("RECIFE", "PE", "81", "2611606", "2531", "50030"),
            new CidadeInfo("FORTALEZA", "CE", "85", "2304400", "1389", "60025"),
            new CidadeInfo("BRASILIA", "DF", "61", "5300108", "9701", "70040"),
            new CidadeInfo("GOIANIA", "GO", "62", "5208707", "9373", "74003"),
            new CidadeInfo("MANAUS", "AM", "92", "1302603", "0255", "69005"),
            new CidadeInfo("BELEM", "PA", "91", "1501402", "0427", "66010")
    );

    private static final List<String> TIPOS_LOGRADOURO = List.of(
            "AVENIDA", "RUA", "ALAMEDA", "PRACA", "TRAVESSA", "RODOVIA"
    );

    private static final List<String> BAIRROS = List.of(
            "CENTRO", "JARDIM PAULISTA", "BELA VISTA", "VILA MARIANA", "PINHEIROS",
            "SANTO AMARO", "MOEMA", "BOTAFOGO", "COPACABANA", "TIJUCA",
            "SAVASSI", "LOURDES", "BATEL", "MOINHOS DE VENTO", "BOA VIAGEM",
            "MEIRELES", "ASA SUL", "ASA NORTE", "SETOR BUENO", "CENTRO CIVICO",
            "ALPHAVILLE CENTRO INDUSTRIAL E EMPRESARIAL"
    );

    public CidadeInfo getRandomCidade() {
        return CIDADES.get(ThreadLocalRandom.current().nextInt(CIDADES.size()));
    }

    public String getRandomTipoLogradouro() {
        return TIPOS_LOGRADOURO.get(ThreadLocalRandom.current().nextInt(TIPOS_LOGRADOURO.size()));
    }

    public String getRandomBairro() {
        return BAIRROS.get(ThreadLocalRandom.current().nextInt(BAIRROS.size()));
    }
}
