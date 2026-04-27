package com.consultores.optiplant.aptiplantback.dto.request;

import com.consultores.optiplant.aptiplantback.enums.TipoMovimiento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MovimientoRequest(
        @NotNull TipoMovimiento tipo,
        @NotNull @DecimalMin("0.01") BigDecimal cantidad,
        String motivo,
        BigDecimal precioUnitario
) {}
