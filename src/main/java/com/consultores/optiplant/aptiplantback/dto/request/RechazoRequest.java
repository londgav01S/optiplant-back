package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RechazoRequest(
        @NotBlank String motivo
) {}
