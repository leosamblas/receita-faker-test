package com.faker.receita.application.common.util;

import java.text.Normalizer;

public final class TextSanitizer {

    private TextSanitizer() {}

    /**
     * Remove acentos diacríticos (NFD), caracteres especiais supérfluos,
     * aplica trim e converte para maiúsculo.
     */
    public static String sanitizeToUpper(String input) {
        if (input == null) {
            return "";
        }
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .trim()
                .toUpperCase();
    }

    /**
     * Sanitiza texto para uso em endereços de e-mail seguros (alfanumérico minúsculo).
     */
    public static String cleanForEmail(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "contato";
        }
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .replaceAll("[^a-zA-Z0-9]", "")
                .toLowerCase();
    }
}
