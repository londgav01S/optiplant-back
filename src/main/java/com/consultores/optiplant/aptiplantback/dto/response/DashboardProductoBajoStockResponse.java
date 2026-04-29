package com.consultores.optiplant.aptiplantback.dto.response;

import java.math.BigDecimal;

/**
 * DTO para la respuesta de métricas de dashboard.
 */
public record DashboardProductoBajoStockResponse(
        String nombre,
        String sucursal,
        BigDecimal stockActual,
        BigDecimal stockMinimo,
        BigDecimal stockMaximo
) {
}
