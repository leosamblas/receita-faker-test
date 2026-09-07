package com.faker.receita.application.common.data;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

@Component
public class AddressGenerator {

    /**
     * Gera um complemento realista para endereços residenciais ou comerciais.
     */
    public String generateComplemento(ThreadLocalRandom random) {
        int compChoice = random.nextInt(5);
        return switch (compChoice) {
            case 0 -> "APTO " + random.nextInt(10, 150) + " BL " + (char) ('A' + random.nextInt(6));
            case 1 -> "SALA " + random.nextInt(100, 900);
            case 2 -> "CASA " + random.nextInt(1, 20);
            case 3 -> "ANDAR " + random.nextInt(1, 20) + " SALA " + random.nextInt(100, 800);
            default -> null;
        };
    }

    /**
     * Gera um número de logradouro aleatório.
     */
    public String generateNumero(ThreadLocalRandom random) {
        return String.valueOf(random.nextInt(10, 3500));
    }

    /**
     * Formata um CEP de 8 dígitos para o padrão 00000-000.
     */
    public String formatCep(String cep8Digits) {
        if (cep8Digits == null || cep8Digits.length() != 8) {
            return cep8Digits;
        }
        return cep8Digits.substring(0, 5) + "-" + cep8Digits.substring(5);
    }
}
