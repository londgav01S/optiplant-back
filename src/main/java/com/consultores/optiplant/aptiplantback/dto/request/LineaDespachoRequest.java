package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record LineaDespachoRequest(
    @NotNull Long idDetalle,
    @NotNull @DecimalMin(value = "0.0") BigDecimal cantidadDespachada
) {
}

