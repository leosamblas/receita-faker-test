# 🚀 DataFaker API

API reativa e de alta performance desenvolvida em **Java 25** e **Spring Boot 4 (WebFlux)** para geração, validação e persistência de dados cadastrais fictícios e consistentes de **Pessoa Física (PF)** e **Pessoa Jurídica (PJ)** no padrão brasileiro (incluindo o **Novo CNPJ Alfanumérico** da Receita Federal).

---

## 📌 Sumário

- [Visão Geral](#-visão-geral)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Arquitetura e Recursos](#-arquitetura-e-recursos)
- [Novo Padrão de CNPJ Alfanumérico](#-novo-padrão-de-cnpj-alfanumérico)
- [Endpoints da API](#-endpoints-da-api)
  - [Pessoa Física (PF)](#1-pessoa-física-pf)
  - [Pessoa Jurídica (PJ)](#2-pessoa-jurídica-pj)
- [Estruturas de Payload (Responses)](#-estruturas-de-payload-responses)
  - [Payload de Pessoa Física](#payload-pessoa-física-pessoafisicaresponse)
  - [Payload de Pessoa Jurídica](#payload-pessoa-jurídica-pessoajuridicaresponse)
- [Como Executar](#-como-executar)
  - [Pré-requisitos](#pré-requisitos)
  - [Subindo o MongoDB](#subindo-o-mongodb-via-docker)
  - [Executando a Aplicação](#executando-a-aplicação)
  - [Executando os Testes](#executando-os-testes)
- [Documentação Swagger / OpenAPI](#-documentação-swagger--openapi)
- [Observabilidade e Métricas](#-observabilidade-e-métricas)
- [Estrutura do Projeto](#-estrutura-do-projeto)

---

## 🎯 Visão Geral

O **DataFaker** foi projetado para atender cenários de testes integrados, homologação, desenvolvimento e simulação de dados em ecossistemas de microsserviços.

### Principais Destaques:
- **Totalmente Não-Bloqueante (Reativo)**: Pipeline reativa de ponta a ponta com **Spring WebFlux** e **Reactive MongoDB**.
- **Virtual Threads Ativadas**: Suporte a Project Loom (`spring.threads.virtual.enabled=true`).
- **Validação e Cálculo Real de CPF e CNPJ**: Algoritmos oficiais da Receita Federal do Brasil (módulo 11) com validação de dígitos verificadores e rejeição de sequências repetidas.
- **Suporte ao Novo CNPJ Alfanumérico**: Compatibilidade total com a nova regulamentação da RFB/SERPRO para CNPJs com raiz e filial alfanuméricas e cálculo Módulo 11 ASCII.
- **Consistência Geográfica e Cadastral**: Banco interno de municípios com códigos oficiais **IBGE** e **TOM**, vinculando DDD, CEP base e estado (UF).
- **Idempotência com Persistência Reativa**: Quando um CPF ou CNPJ é gerado ou consultado, ele é persistido no MongoDB (`pessoas_fisicas` e `pessoas_juridicas`). Consultas subsequentes pelo mesmo documento retornam os dados já salvos, garantindo consistência durante fluxos de teste.
- **Tratamento de Fusos Horários (`ZoneId`)**: Manipulação de datas configurada explicitamente para `America/Sao_Paulo`.

---

## 🛠 Tecnologias Utilizadas

- **Java 25** (com `--enable-native-access=ALL-UNNAMED`)
- **Spring Boot 4.1.1**
  - Spring WebFlux (Project Reactor)
  - Spring Data Reactive MongoDB
  - Spring Boot Actuator
  - Spring Boot Validation
- **MongoDB** (Persistência reativa)
- **Net Datafaker 2.7.0** (Geração de nomes, logradouros e textos fakes)
- **SpringDoc OpenAPI 3.1.0** (Documentação interativa OpenAPI 3 / Swagger UI)
- **Project Lombok**
- **JUnit 5 / Mockito / Reactor Test (StepVerifier)**
- **Gradle**

---

## 🏛️ Novo Padrão de CNPJ Alfanumérico

A API está pronta para o **Novo CNPJ Alfanumérico** da Receita Federal do Brasil:
- **Estrutura (14 posições)**:
  - **Raiz (posições 1 a 8)**: Alfanumérica (`0-9`, `A-Z`).
  - **Ordem/Filial (posições 9 a 12)**: Alfanumérica (`0-9`, `A-Z`).
  - **Dígitos Verificadores (posições 13 e 14)**: Estritamente numéricos (`0-9`).
- **Cálculo Módulo 11 Alfanumérico**: Conversão de cada caractere para valor numérico via tabela ASCII (`código_ascii - 48`) e multiplicação pelos pesos oficiais `[5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]` e `[6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]`.
- **Compatibilidade Híbrida**: Aceita tanto o formato tradicional 100% numérico quanto o alfanumérico, com ou sem formatação.

---

## 🌐 Endpoints da API

A documentação interativa com Swagger pode ser acessada em:  
👉 **`http://localhost:8080/swagger-ui.html`**

### 1. Pessoa Física (PF)

#### 1.1 Gerar Pessoa Física Aleatória
Gera uma nova Pessoa Física com CPF válido inédito, dados cadastrais completos, endereço e contatos, persistindo-a no banco.

- **Método**: `GET`
- **URL**: `/api/v1/pf`
- **Resposta**: `200 OK`

```bash
curl -X GET http://localhost:8080/api/v1/pf
```

#### 1.2 Gerar ou Obter Pessoa Física por CPF
Busca a Pessoa Física salva no MongoDB para o CPF fornecido (com ou sem máscara). Se não existir, gera dados completos consistentes para aquele CPF e salva no banco.

- **Método**: `GET`
- **URL**: `/api/v1/pf/{cpf}`
- **Respostas**: `200 OK` (encontrado/gerado) | `400 Bad Request` (inválido)

```bash
# CPF sem máscara
curl -X GET http://localhost:8080/api/v1/pf/23456789012

# CPF formatado
curl -X GET http://localhost:8080/api/v1/pf/234.567.890-12
```

---

### 2. Pessoa Jurídica (PJ)

#### 2.1 Gerar Pessoa Jurídica Aleatória
Gera uma nova Pessoa Jurídica completa com CNPJ válido aleatório (numérico tradicional ou novo alfanumérico), Razão Social, Nome Fantasia, CNAE principal e secundários, QSA e Regime Tributário.

- **Método**: `GET`
- **URL**: `/api/v1/pj`
- **Resposta**: `200 OK`

```bash
curl -X GET http://localhost:8080/api/v1/pj
```

#### 2.2 Gerar ou Obter Pessoa Jurídica por CNPJ
Busca a Pessoa Jurídica no MongoDB pelo CNPJ informado (com ou sem máscara, tradicional ou alfanumérico). Se não existir, valida e gera dados correspondentes, persistindo-os.

- **Método**: `GET`
- **URL**: `/api/v1/pj/{cnpj}`
- **Respostas**: `200 OK` (encontrado/gerado) | `400 Bad Request` (inválido)

```bash
# CNPJ tradicional sem máscara
curl -X GET http://localhost:8080/api/v1/pj/04740876000125

# CNPJ formatado
curl -X GET http://localhost:8080/api/v1/pj/04.740.876/0001-25
```

---

## 📋 Estruturas de Payload (Responses)

### Payload: Pessoa Física (`PessoaFisicaResponse`)

```json
{
  "dadosCadastrais": {
    "numeroCpf": "94265438090",
    "cpfFormatado": "942.654.380-90",
    "digitoVerificador": "90",
    "nomePessoaFisica": "ANA CLARA SILVA SANTOS",
    "nomeSocial": null,
    "dataNascimento": "1988-04-12",
    "sexo": "F",
    "nacionalidade": "BRASILEIRA",
    "paisNascimento": "BRASIL",
    "nomeMae": "MARIA SILVA DE OLIVEIRA",
    "tituloEleitor": "083726194028",
    "dataObito": null
  },
  "situacaoReceita": {
    "codigo": "0",
    "descricao": "REGULAR",
    "dataInscricao": "2006-08-15",
    "dataConsulta": "2006-08-15"
  },
  "endereco": {
    "cep": "01001000",
    "cepFormatado": "01001-000",
    "tipoLogradouro": "RUA",
    "logradouro": "DAS FLORES",
    "numero": "420",
    "complemento": "APTO 42 BL B",
    "bairro": "CENTRO",
    "municipio": "SAO PAULO",
    "codigoMunicipioIbge": "3550308",
    "codigoMunicipioTom": "7107",
    "uf": "SP",
    "pais": "BRASIL"
  },
  "contato": {
    "telefones": [
      {
        "tipo": "CELULAR",
        "ddd": "11",
        "numero": "987654321"
      },
      {
        "tipo": "FIXO",
        "ddd": "11",
        "numero": "32145678"
      }
    ],
    "emails": [
      {
        "tipo": "PRINCIPAL",
        "email": "ana.santos@email.com"
      },
      {
        "tipo": "ALTERNATIVO",
        "email": "ana.contato@empresa.com.br"
      }
    ]
  }
}
```

### Payload: Pessoa Jurídica (`PessoaJuridicaResponse`)

```json
{
  "cnpj": "04740876000125",
  "razaoSocial": "ALELO S.A.",
  "nomeFantasia": "ALELO",
  "uf": "SP",
  "cep": "06455030",
  "bairro": "ALPHAVILLE CENTRO INDUSTRIAL E EMPRESARIAL/ALPHAV",
  "logradouro": "XINGU",
  "numero": "512",
  "complemento": "ANDAR 3 E 4 E 16 PARTE",
  "municipio": "BARUERI",
  "codigoMunicipio": 6213,
  "codigoMunicipioIbge": 3505708,
  "porte": "DEMAIS",
  "capitalSocial": 472414100.0,
  "naturezaJuridica": "Sociedade Anônima Fechada",
  "cnaeFiscal": 8299702,
  "cnaeFiscalDescricao": "Emissão de vales-alimentação, vales-transporte e similares",
  "descricaoSituacaoCadastral": "ATIVA",
  "qsa": [
    {
      "nomeSocio": "ANA JULIA DE VASCONCELOS CAREPA",
      "qualificacaoSocio": "Conselheiro de Administração",
      "faixaEtaria": "Entre 61 a 70 anos"
    }
  ],
  "cnaesSecundarios": [
    {
      "codigo": 6619302,
      "descricao": "Correspondentes de instituições financeiras"
    }
  ],
  "regimeTributario": [
    {
      "ano": 2024,
      "formaDeTributacao": "LUCRO REAL",
      "quantidadeDeEscrituracoes": 1
    }
  ]
}
```

---

## ⚙️ Como Executar

### Pré-requisitos

- **Java 25** instalado e configurado no `PATH`
- **Docker** ou instância do **MongoDB** em execução

### Subindo o MongoDB via Docker

Você pode iniciar uma instância do MongoDB compatível com as credenciais padrão do `application.properties`:

```bash
docker run -d \
  --name datafaker-mongo \
  -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=root \
  -e MONGO_INITDB_ROOT_PASSWORD=developer \
  mongo:latest
```

### Executando a Aplicação

Pelo Gradle Wrapper:

```bash
# No Linux / macOS
./gradlew bootRun

# No Windows (PowerShell / CMD)
.\gradlew.bat bootRun
```

A aplicação subirá por padrão na porta **8080**.

### Executando os Testes

Os testes unitários e de integração reativos utilizam `StepVerifier`, Mockito e WebTestClient:

```bash
# No Linux / macOS
./gradlew test

# No Windows
.\gradlew.bat test
```

---

## 📖 Documentação Swagger / OpenAPI

Com a aplicação rodando, acesse os links de documentação da API:

| Recurso | URL |
|---|---|
| **Swagger UI** | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |
| **OpenAPI Docs (JSON)** | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) |

---

## 📊 Observabilidade e Métricas

A aplicação inclui o **Spring Boot Actuator** com probes de integridade configuradas:

- **Health Check**: `GET http://localhost:8080/actuator/health`
- **Liveness State**: `GET http://localhost:8080/actuator/health/liveness`
- **Readiness State**: `GET http://localhost:8080/actuator/health/readiness`
- **Métricas Prometheus**: `GET http://localhost:8080/actuator/prometheus`

---

## 📂 Estrutura do Projeto (Clean Architecture)

A aplicação segue rigorosamente os princípios de **Clean Architecture**, isolando o domínio das regras de aplicação e adaptadores de infraestrutura:

```text
datafaker/
├── build.gradle
├── settings.gradle
├── src/
│   ├── main/
│   │   ├── java/com/faker/receita/
│   │   │   ├── DatafakerApplication.java
│   │   │   │
│   │   │   ├── domain/                               # NÚCLEO DE DOMÍNIO (Zero Spring/Libs)
│   │   │   │   ├── exception/                        # Exceções puras de negócio
│   │   │   │   │   ├── DomainException.java
│   │   │   │   │   └── InvalidDocumentException.java
│   │   │   │   ├── model/
│   │   │   │   │   ├── pf/                           # Entidades de Pessoa Física
│   │   │   │   │   │   ├── PessoaFisica.java
│   │   │   │   │   │   ├── DadosCadastrais.java
│   │   │   │   │   │   ├── SituacaoReceita.java
│   │   │   │   │   │   ├── Endereco.java
│   │   │   │   │   │   └── Contato.java (Telefone, Email)
│   │   │   │   │   └── pj/                           # Entidades de Pessoa Jurídica
│   │   │   │   │       ├── PessoaJuridica.java
│   │   │   │   │       ├── Socio.java
│   │   │   │   │       ├── CnaeSecundario.java
│   │   │   │   │       └── RegimeTributario.java
│   │   │   │   └── validation/                       # Validações de Domínio (Módulo 11)
│   │   │   │       ├── DocumentValidator.java        # Strategy de validação
│   │   │   │       ├── CpfValidator.java
│   │   │   │       └── CnpjValidator.java
│   │   │   │
│   │   │   ├── application/                          # CASOS DE USO E PORTAS
│   │   │   │   ├── common/
│   │   │   │   │   ├── data/                         # Base de apoio (IBGE/TOM, AddressGenerator)
│   │   │   │   │   └── util/                         # TextSanitizer (regras de texto puro)
│   │   │   │   ├── pf/
│   │   │   │   │   ├── port/
│   │   │   │   │   │   ├── in/PessoaFisicaUseCase.java
│   │   │   │   │   │   └── out/PessoaFisicaRepositoryPort.java
│   │   │   │   │   └── service/PessoaFisicaService.java
│   │   │   │   └── pj/
│   │   │   │       ├── port/
│   │   │   │       │   ├── in/PessoaJuridicaUseCase.java
│   │   │   │       │   └── out/PessoaJuridicaRepositoryPort.java
│   │   │   │       └── service/PessoaJuridicaService.java
│   │   │   │
│   │   │   └── infrastructure/                       # ADAPTADORES E FRAMEWORKS
│   │   │       ├── config/                           # Configurações Spring (Clock, OpenAPI, Faker)
│   │   │       ├── persistence/mongodb/              # Adaptador de Saída (MongoDB Reativo)
│   │   │       │   ├── document/                     # Coleções @Document
│   │   │       │   ├── repository/                   # Repositórios Spring Data
│   │   │       │   └── adapter/                      # Implementações das Portas de Saída
│   │   │       └── web/                              # Adaptador de Entrada (REST WebFlux)
│   │   │           ├── controller/                   # Endpoints /api/v1/pf e /api/v1/pj
│   │   │           ├── dto/                          # DTOs de Contrato REST
│   │   │           └── exception/                    # GlobalExceptionHandler e ErrorResponse
│   │   └── resources/
│   │       └── application.properties                # Configurações 12-Factor App
│   └── test/                                         # Testes com JUnit 5, Mockito e StepVerifier
└── README.md
```

---

## 📄 Licença

Projeto desenvolvido para fins de desenvolvimento, mock e testes de software.
