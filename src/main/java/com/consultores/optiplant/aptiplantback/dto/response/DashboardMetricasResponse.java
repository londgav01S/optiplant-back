package com.consultores.optiplant.aptiplantback.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para la respuesta de métricas de dashboard.
 */
public record DashboardMetricasResponse(
        BigDecimal ventasMes,
        BigDecimal comprasMes,
        Long productosBajoStockCount,
        Long transferenciasPendientes,
        BigDecimal stockTotal,
        List<DashboardProductoBajoStockResponse> productosBajoStock,
        List<DashboardVentaMensualChartResponse> ventasMensuales
) {
}
