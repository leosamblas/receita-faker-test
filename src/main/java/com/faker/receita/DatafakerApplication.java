package com.faker.receita;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableReactiveMongoAuditing
@EnableReactiveMongoRepositories(basePackages = "com.faker.receita.infrastructure.persistence.mongodb.repository")
public class DatafakerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatafakerApplication.class, args);
    }
}
