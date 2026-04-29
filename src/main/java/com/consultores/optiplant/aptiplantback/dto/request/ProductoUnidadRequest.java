package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * DTO para la solicitud de asociación de un producto con una unidad de medida.
 */
public record ProductoUnidadRequest(
    @NotNull Long idUnidad,
    @NotNull Boolean esPrincipal,
    @NotNull @Positive BigDecimal factorConversion
) {
}

