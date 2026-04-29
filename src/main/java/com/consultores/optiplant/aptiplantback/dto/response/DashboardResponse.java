package com.consultores.optiplant.aptiplantback.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
    BigDecimal ventasDelDia,
    BigDecimal ventasDelMes,
    BigDecimal comprasMes,
    Long alertasActivas,
    Long transferenciasPendientes,
    Long ordenesCompraPendientes,
    BigDecimal stockTotal,
    List<VentaMensualResponse> ventasMensuales,
    Long productosBajoStockMinimo
) {
    public static DashboardResponse sucursal(BigDecimal ventasDelDia, BigDecimal ventasDelMes,
                                              BigDecimal comprasMes,
                                              Long alertasActivas, Long transferenciasPendientes,
                                              Long ordenesCompraPendientes, BigDecimal stockTotal,
                                              List<VentaMensualResponse> ventasMensuales) {
        return new DashboardResponse(ventasDelDia, ventasDelMes, comprasMes, alertasActivas,
                transferenciasPendientes, ordenesCompraPendientes, stockTotal, ventasMensuales, null);
    }
}
