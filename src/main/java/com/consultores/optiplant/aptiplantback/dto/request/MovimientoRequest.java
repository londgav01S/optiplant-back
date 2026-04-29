package com.consultores.optiplant.aptiplantback.dto.request;

import com.consultores.optiplant.aptiplantback.enums.TipoMovimiento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO para la solicitud de movimiento de inventario.
 */
public record MovimientoRequest(
        @NotNull TipoMovimiento tipo,
        @NotNull @DecimalMin("0.01") BigDecimal cantidad,
        String motivo,
        BigDecimal precioUnitario
) {}
