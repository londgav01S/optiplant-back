package com.consultores.optiplant.aptiplantback.dto.response;

import java.math.BigDecimal;

public record DashboardVentaMensualChartResponse(
        String name,
        BigDecimal ventas
) {
}
