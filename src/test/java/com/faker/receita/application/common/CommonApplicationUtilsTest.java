package com.faker.receita.application.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;

import com.faker.receita.application.common.data.AddressGenerator;
import com.faker.receita.application.common.util.TextSanitizer;

class CommonApplicationUtilsTest {

    @Test
    void shouldSanitizeTextCorrectly() {
        assertEquals("JOSE DA SILVA", TextSanitizer.sanitizeToUpper("José da Silva"));
        assertEquals("SAO PAULO", TextSanitizer.sanitizeToUpper("São Paulo"));
        assertEquals("", TextSanitizer.sanitizeToUpper(null));
        assertEquals("josedasilva", TextSanitizer.cleanForEmail("José da Silva!"));
        assertEquals("contato", TextSanitizer.cleanForEmail(null));
    }

    @Test
    void shouldGenerateAddressFields() {
        AddressGenerator generator = new AddressGenerator();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        assertNotNull(generator.generateNumero(random));
        assertEquals("01310-000", generator.formatCep("01310000"));
    }
}
