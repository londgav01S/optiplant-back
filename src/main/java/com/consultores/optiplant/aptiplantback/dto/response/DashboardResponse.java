package com.consultores.optiplant.aptiplantback.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
    BigDecimal ventasDelDia,
    BigDecimal ventasDelMes,
    Long alertasActivas,
    Long transferenciasPendientes,
    Long ordenesCompraPendientes,
    // Solo para dashboardGlobal()
    List<VentaMensualResponse> ventasMensuales,
    Long productosBajoStockMinimo
) {
    // Factory para dashboard de sucursal (sin métricas globales)
    public static DashboardResponse sucursal(BigDecimal ventasDelDia, BigDecimal ventasDelMes,
                                              Long alertasActivas, Long transferenciasPendientes,
                                              Long ordenesCompraPendientes) {
        return new DashboardResponse(ventasDelDia, ventasDelMes, alertasActivas,
                transferenciasPendientes, ordenesCompraPendientes, null, null);
    }
}
