package com.faker.receita.infrastructure.web.dto.pf;

import java.util.List;

public record ContatoDto(
        List<TelefoneDto> telefones,
        List<EmailDto> emails
) {}
