package com.faker.receita.domain.validation;

import java.util.Random;

import com.faker.receita.domain.exception.InvalidDocumentException;

/**
 * Validador e gerador oficial de CNPJ pelo algoritmo de Módulo 11 da Receita Federal.
 * Suporta o formato numérico clássico e o Novo Padrão Alfanumérico da RFB/SERPRO.
 * Classe pura de domínio sem dependência de frameworks.
 */
public class CnpjValidator implements DocumentValidator {

    private static final int[] WEIGHTS_DV1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] WEIGHTS_DV2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final String ALPHANUMERIC_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final Random random = new Random();

    @Override
    public String cleanAndValidate(String cnpj) {
        if (cnpj == null || cnpj.trim().isEmpty()) {
            throw new InvalidDocumentException("CNPJ não pode ser vazio ou nulo.");
        }

        String clean = cnpj.toUpperCase().replaceAll("[^0-9A-Z]", "");

        if (clean.length() != 14) {
            throw new InvalidDocumentException(String.format("CNPJ '%s' deve conter exatamente 14 caracteres.", cnpj));
        }

        if (isAllCharactersIdentical(clean)) {
            throw new InvalidDocumentException(String.format("CNPJ '%s' é inválido (caracteres repetidos).", cnpj));
        }

        if (!Character.isDigit(clean.charAt(12)) || !Character.isDigit(clean.charAt(13))) {
            throw new InvalidDocumentException(
                    String.format("CNPJ '%s' possui dígitos verificadores não numéricos.", cnpj));
        }

        int dv1 = calculateFirstCheckDigit(clean);
        if (Character.getNumericValue(clean.charAt(12)) != dv1) {
            throw new InvalidDocumentException(
                    String.format("CNPJ '%s' é inválido no primeiro dígito verificador.", cnpj));
        }

        int dv2 = calculateSecondCheckDigit(clean, dv1);
        if (Character.getNumericValue(clean.charAt(13)) != dv2) {
            throw new InvalidDocumentException(
                    String.format("CNPJ '%s' é inválido no segundo dígito verificador.", cnpj));
        }

        return clean;
    }

    @Override
    public boolean isValid(String cnpj) {
        try {
            cleanAndValidate(cnpj);
            return true;
        } catch (InvalidDocumentException e) {
            return false;
        }
    }

    @Override
    public String format(String cleanCnpj) {
        if (cleanCnpj == null || cleanCnpj.length() != 14) {
            throw new IllegalArgumentException("CNPJ deve conter exatamente 14 caracteres para formatação.");
        }
        return String.format("%s.%s.%s/%s-%s",
                cleanCnpj.substring(0, 2),
                cleanCnpj.substring(2, 5),
                cleanCnpj.substring(5, 8),
                cleanCnpj.substring(8, 12),
                cleanCnpj.substring(12, 14));
    }

    @Override
    public String generateValidDocument() {
        return generateValidCnpj();
    }

    public String generateValidCnpj() {
        return random.nextBoolean() ? generateValidNumericCnpj() : generateValidAlphanumericCnpj();
    }

    public String generateValidNumericCnpj() {
        while (true) {
            StringBuilder base = new StringBuilder(12);
            for (int i = 0; i < 8; i++) {
                base.append(random.nextInt(10));
            }
            base.append("0001");

            String baseStr = base.toString();
            if (isAllCharactersIdentical(baseStr)) {
                continue;
            }

            int dv1 = calculateFirstCheckDigit(baseStr);
            int dv2 = calculateSecondCheckDigit(baseStr, dv1);

            return baseStr + dv1 + dv2;
        }
    }

    public String generateValidAlphanumericCnpj() {
        while (true) {
            StringBuilder base = new StringBuilder(12);
            for (int i = 0; i < 8; i++) {
                base.append(ALPHANUMERIC_CHARS.charAt(random.nextInt(ALPHANUMERIC_CHARS.length())));
            }
            if (random.nextBoolean()) {
                base.append("0001");
            } else {
                for (int i = 0; i < 4; i++) {
                    base.append(ALPHANUMERIC_CHARS.charAt(random.nextInt(ALPHANUMERIC_CHARS.length())));
                }
            }

            String baseStr = base.toString();
            if (isAllCharactersIdentical(baseStr)) {
                continue;
            }

            int dv1 = calculateFirstCheckDigit(baseStr);
            int dv2 = calculateSecondCheckDigit(baseStr, dv1);

            return baseStr + dv1 + dv2;
        }
    }

    private int getCharacterValue(char c) {
        return ((int) c) - 48;
    }

    private int calculateFirstCheckDigit(String base12) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += getCharacterValue(base12.charAt(i)) * WEIGHTS_DV1[i];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    private int calculateSecondCheckDigit(String base12, int dv1) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += getCharacterValue(base12.charAt(i)) * WEIGHTS_DV2[i];
        }
        sum += dv1 * WEIGHTS_DV2[12];
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    private boolean isAllCharactersIdentical(String s) {
        char first = s.charAt(0);
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) != first) {
                return false;
            }
        }
        return true;
    }
}
