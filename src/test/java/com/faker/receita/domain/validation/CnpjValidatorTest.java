package com.faker.receita.domain.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.faker.receita.domain.exception.InvalidDocumentException;
import com.faker.receita.domain.validation.CnpjValidator;

class CnpjValidatorTest {

    private CnpjValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CnpjValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "04740876000125",
            "04.740.876/0001-25",
            "11.222.333/0001-81",
            "11222333000181"
    })
    @DisplayName("Deve validar com sucesso CNPJs numéricos válidos (com e sem formatação)")
    void shouldValidateValidNumericCnpjs(String cnpj) {
        assertTrue(validator.isValid(cnpj));
        String clean = validator.cleanAndValidate(cnpj);
        assertEquals(14, clean.length());
    }

    @Test
    @DisplayName("Deve validar CNPJ alfanumérico com DVs calculados pela tabela ASCII")
    void shouldValidateAlphanumericCnpj() {
        String generatedAlpha = validator.generateValidAlphanumericCnpj();
        assertNotNull(generatedAlpha);
        assertEquals(14, generatedAlpha.length());
        assertTrue(validator.isValid(generatedAlpha));

        String formatted = validator.format(generatedAlpha);
        assertTrue(validator.isValid(formatted));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "123",
            "00000000000000",
            "11111111111111",
            "04740876000124",
            "04740876000195",
            "0474087600012A",
            "123456789012345"
    })
    @DisplayName("Deve rejeitar CNPJs inválidos lançando InvalidDocumentException")
    void shouldThrowInvalidDocumentExceptionForInvalidCnpjs(String invalidCnpj) {
        assertFalse(validator.isValid(invalidCnpj));
        assertThrows(InvalidDocumentException.class, () -> validator.cleanAndValidate(invalidCnpj));
    }

    @Test
    @DisplayName("Deve lançar InvalidDocumentException para CNPJ nulo")
    void shouldThrowForNullCnpj() {
        assertFalse(validator.isValid(null));
        assertThrows(InvalidDocumentException.class, () -> validator.cleanAndValidate(null));
    }

    @Test
    @DisplayName("Deve formatar CNPJ de 14 dígitos corretamente")
    void shouldFormatCnpjCorrectly() {
        String formatted = validator.format("04740876000125");
        assertEquals("04.740.876/0001-25", formatted);
    }

    @Test
    @DisplayName("Deve gerar CNPJs aleatórios válidos de forma contínua")
    void shouldGenerateValidRandomCnpjsConsistently() {
        for (int i = 0; i < 50; i++) {
            String cnpj = validator.generateValidCnpj();
            assertEquals(14, cnpj.length());
            assertTrue(validator.isValid(cnpj), "CNPJ gerado deve ser válido: " + cnpj);
        }
    }
}
