package com.faker.receita.infrastructure.config;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.datafaker.Faker;

@Configuration
public class FakerConfig {

    @Bean
    public Faker faker() {
        return new Faker(Locale.of("pt", "BR"));
    }
}
