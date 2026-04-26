package com.consultores.optiplant.aptiplantback.dto.request;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record InventarioConfigRequest(
    @DecimalMin(value = "0.0") BigDecimal stockMinimo,
    @DecimalMin(value = "0.0") BigDecimal stockMaximo
) {
}

