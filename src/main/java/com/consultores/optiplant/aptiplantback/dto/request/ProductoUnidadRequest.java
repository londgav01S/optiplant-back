package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ProductoUnidadRequest(
    @NotNull Long idUnidad,
    @NotNull Boolean esPrincipal,
    @NotNull @Positive BigDecimal factorConversion
) {
}

