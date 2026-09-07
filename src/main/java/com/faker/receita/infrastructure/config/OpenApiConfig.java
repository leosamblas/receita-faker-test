package com.faker.receita.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DataFaker API")
                        .version("1.0.0")
                        .description("API reativa e de alta performance desenvolvida em Java 25 e Spring WebFlux "
                                + "para geração, validação e persistência de dados cadastrais fictícios de Pessoa Física (PF) "
                                + "e Pessoa Jurídica (PJ), incluindo suporte ao Novo Padrão de CNPJ Alfanumérico da Receita Federal.")
                        .contact(new Contact()
                                .name("DataFaker Team")
                                .url("https://github.com/datafaker"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
