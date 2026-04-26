package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record VentaRequest(
    @NotNull Long idSucursal,
    Long idListaPrecios,
    @DecimalMin(value = "0.0") @DecimalMax(value = "100.0") BigDecimal descuentoGlobal,
    @NotEmpty List<@Valid LineaVentaRequest> lineas
) {
}

