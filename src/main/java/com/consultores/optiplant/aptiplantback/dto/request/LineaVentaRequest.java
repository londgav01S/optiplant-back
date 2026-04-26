package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record LineaVentaRequest(
    @NotNull Long idProducto,
    @NotNull @Positive BigDecimal cantidad,
    @DecimalMin(value = "0.0") BigDecimal descuentoLinea
) {
}

