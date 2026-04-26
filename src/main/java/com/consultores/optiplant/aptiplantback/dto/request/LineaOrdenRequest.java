package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record LineaOrdenRequest(
    @NotNull Long idProducto,
    @NotNull @Positive BigDecimal cantidadPedida,
    @NotNull @Positive BigDecimal precioUnitario,
    @DecimalMin(value = "0.0") @DecimalMax(value = "100.0") BigDecimal descuento
) {
}

