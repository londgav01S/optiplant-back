package com.consultores.optiplant.aptiplantback.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * DTO para la solicitud de línea de venta.
 */
public record LineaVentaRequest(
    @JsonAlias({"productoId"}) @NotNull Long idProducto,
    @NotNull @Positive BigDecimal cantidad,
    @DecimalMin(value = "0.0") BigDecimal descuentoLinea
) {
}

