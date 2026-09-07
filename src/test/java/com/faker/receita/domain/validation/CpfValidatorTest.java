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
import com.faker.receita.domain.validation.CpfValidator;

class CpfValidatorTest {

    private CpfValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CpfValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "52998224725",
            "529.982.247-25",
            "11144477735",
            "111.444.777-35"
    })
    @DisplayName("Deve validar com sucesso CPFs válidos (com e sem formatação)")
    void shouldValidateValidCpfs(String cpf) {
        assertTrue(validator.isValid(cpf));
        String clean = validator.cleanAndValidate(cpf);
        assertEquals(11, clean.length());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "123",
            "11111111111",
            "00000000000",
            "52998224724",
            "52998224795",
            "123456789012"
    })
    @DisplayName("Deve rejeitar CPFs inválidos lançando InvalidDocumentException")
    void shouldThrowInvalidDocumentExceptionForInvalidCpfs(String invalidCpf) {
        assertFalse(validator.isValid(invalidCpf));
        assertThrows(InvalidDocumentException.class, () -> validator.cleanAndValidate(invalidCpf));
    }

    @Test
    @DisplayName("Deve lançar InvalidDocumentException para CPF nulo")
    void shouldThrowForNullCpf() {
        assertFalse(validator.isValid(null));
        assertThrows(InvalidDocumentException.class, () -> validator.cleanAndValidate(null));
    }

    @Test
    @DisplayName("Deve formatar CPF de 11 dígitos corretamente")
    void shouldFormatCpfCorrectly() {
        String formatted = validator.format("52998224725");
        assertEquals("529.982.247-25", formatted);
    }

    @Test
    @DisplayName("Deve gerar CPFs aleatórios válidos de forma contínua")
    void shouldGenerateValidRandomCpfsConsistently() {
        for (int i = 0; i < 50; i++) {
            String cpf = validator.generateValidCpf();
            assertNotNull(cpf);
            assertEquals(11, cpf.length());
            assertTrue(validator.isValid(cpf), "CPF gerado deve ser válido: " + cpf);
        }
    }
}
