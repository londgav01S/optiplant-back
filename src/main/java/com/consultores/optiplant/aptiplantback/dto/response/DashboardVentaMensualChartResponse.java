package com.consultores.optiplant.aptiplantback.dto.response;

import java.math.BigDecimal;

/**
 * DTO para la respuesta de métricas de dashboard.
 */
public record DashboardVentaMensualChartResponse(
        String name,
        BigDecimal ventas
) {
}
