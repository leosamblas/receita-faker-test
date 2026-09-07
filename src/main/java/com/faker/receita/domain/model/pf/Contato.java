package com.faker.receita.domain.model.pf;

import java.util.List;

public record Contato(
        List<Telefone> telefones,
        List<Email> emails
) {}
