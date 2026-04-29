package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * DTO para la solicitud de línea de transferencia.
 */
public record LineaTransferenciaRequest(
    @NotNull Long idProducto,
    @NotNull @Positive BigDecimal cantidadSolicitada
) {
}

