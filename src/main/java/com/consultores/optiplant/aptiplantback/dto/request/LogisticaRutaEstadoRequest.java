package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LogisticaRutaEstadoRequest(
        @NotBlank String estado
) {
}
