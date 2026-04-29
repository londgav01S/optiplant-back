package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO para la solicitud de línea de recepción de transferencia.
 */
public record LineaRecepcionTransferenciaRequest(
    @NotNull Long idDetalle,
    @NotNull @DecimalMin(value = "0.0") BigDecimal cantidadRecibida
) {
}

