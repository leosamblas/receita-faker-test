package com.faker.receita.domain.validation;

/**
 * Interface Strategy para validação, sanitização, formatação
 * e geração de documentos brasileiros.
 */
public interface DocumentValidator {

    /**
     * Limpa pontuações/espaços e valida o documento pelas regras oficiais.
     *
     * @param document string do documento com ou sem máscara
     * @return string limpa e normalizada
     */
    String cleanAndValidate(String document);

    /**
     * Verifica se o documento é válido sem lançar exceção.
     */
    boolean isValid(String document);

    /**
     * Aplica a máscara padrão no documento limpo.
     */
    String format(String cleanDocument);

    /**
     * Gera um documento válido aleatório.
     */
    String generateValidDocument();
}
