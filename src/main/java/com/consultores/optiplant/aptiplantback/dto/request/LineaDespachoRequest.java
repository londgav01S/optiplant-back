package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO para la solicitud de línea de despacho.
 */
public record LineaDespachoRequest(
    @NotNull Long idDetalle,
    @NotNull @DecimalMin(value = "0.0") BigDecimal cantidadDespachada
) {
}

