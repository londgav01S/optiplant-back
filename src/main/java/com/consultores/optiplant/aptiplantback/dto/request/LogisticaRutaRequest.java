package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LogisticaRutaRequest(
        @NotNull Long transferenciaId,
        @NotBlank String vehiculo,
        @NotBlank String conductor
) {
}
