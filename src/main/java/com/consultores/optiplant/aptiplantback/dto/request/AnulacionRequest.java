package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AnulacionRequest(
        @NotBlank String motivo
) {}
