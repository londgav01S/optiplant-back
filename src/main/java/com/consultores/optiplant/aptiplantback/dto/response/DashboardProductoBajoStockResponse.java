package com.consultores.optiplant.aptiplantback.dto.response;

import java.math.BigDecimal;

public record DashboardProductoBajoStockResponse(
        String nombre,
        String sucursal,
        BigDecimal stockActual,
        BigDecimal stockMinimo,
        BigDecimal stockMaximo
) {
}
