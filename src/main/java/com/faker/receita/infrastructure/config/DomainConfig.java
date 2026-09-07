package com.faker.receita.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.faker.receita.domain.validation.CnpjValidator;
import com.faker.receita.domain.validation.CpfValidator;

@Configuration
public class DomainConfig {

    @Bean
    public CpfValidator cpfValidator() {
        return new CpfValidator();
    }

    @Bean
    public CnpjValidator cnpjValidator() {
        return new CnpjValidator();
    }
}
