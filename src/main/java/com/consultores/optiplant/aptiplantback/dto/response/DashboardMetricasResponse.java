package com.consultores.optiplant.aptiplantback.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardMetricasResponse(
        BigDecimal ventasMes,
        BigDecimal comprasMes,
        Long productosBajoStockCount,
        Long transferenciasPendientes,
        List<DashboardProductoBajoStockResponse> productosBajoStock,
        List<DashboardVentaMensualChartResponse> ventasMensuales
) {
}
