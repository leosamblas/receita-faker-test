package com.faker.receita.domain.validation;

import java.util.Random;

import com.faker.receita.domain.exception.InvalidDocumentException;

/**
 * Validador e gerador oficial de CPF pelo algoritmo de Módulo 11 da Receita Federal.
 * Classe pura de domínio sem dependência de frameworks.
 */
public class CpfValidator implements DocumentValidator {

    private final Random random = new Random();

    @Override
    public String cleanAndValidate(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new InvalidDocumentException("CPF não pode ser vazio ou nulo.");
        }

        String digits = cpf.replaceAll("\\D", "");

        if (digits.length() != 11) {
            throw new InvalidDocumentException(String.format("CPF '%s' deve conter exatamente 11 dígitos.", cpf));
        }

        if (isAllDigitsIdentical(digits)) {
            throw new InvalidDocumentException(String.format("CPF '%s' é inválido (dígitos repetidos).", cpf));
        }

        int dv1 = calculateFirstCheckDigit(digits);
        if (Character.getNumericValue(digits.charAt(9)) != dv1) {
            throw new InvalidDocumentException(String.format("CPF '%s' é inválido no primeiro dígito verificador.", cpf));
        }

        int dv2 = calculateSecondCheckDigit(digits, dv1);
        if (Character.getNumericValue(digits.charAt(10)) != dv2) {
            throw new InvalidDocumentException(String.format("CPF '%s' é inválido no segundo dígito verificador.", cpf));
        }

        return digits;
    }

    @Override
    public boolean isValid(String cpf) {
        try {
            cleanAndValidate(cpf);
            return true;
        } catch (InvalidDocumentException e) {
            return false;
        }
    }

    @Override
    public String format(String digits) {
        if (digits == null || digits.length() != 11) {
            throw new IllegalArgumentException("Dígitos de CPF devem conter exatamente 11 caracteres.");
        }
        return String.format("%s.%s.%s-%s",
                digits.substring(0, 3),
                digits.substring(3, 6),
                digits.substring(6, 9),
                digits.substring(9, 11));
    }

    @Override
    public String generateValidDocument() {
        return generateValidCpf();
    }

    public String generateValidCpf() {
        while (true) {
            StringBuilder base = new StringBuilder(9);
            for (int i = 0; i < 9; i++) {
                base.append(random.nextInt(10));
            }
            String baseStr = base.toString();
            if (isAllDigitsIdentical(baseStr)) {
                continue;
            }

            int dv1 = calculateFirstCheckDigit(baseStr);
            int dv2 = calculateSecondCheckDigit(baseStr, dv1);

            return baseStr + dv1 + dv2;
        }
    }

    private boolean isAllDigitsIdentical(String s) {
        char first = s.charAt(0);
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) != first) {
                return false;
            }
        }
        return true;
    }

    private int calculateFirstCheckDigit(String digits) {
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(digits.charAt(i)) * (10 - i);
        }
        int remainder = (sum * 10) % 11;
        return remainder == 10 ? 0 : remainder;
    }

    private int calculateSecondCheckDigit(String digits, int dv1) {
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(digits.charAt(i)) * (11 - i);
        }
        sum += dv1 * 2;
        int remainder = (sum * 10) % 11;
        return remainder == 10 ? 0 : remainder;
    }
}
