package com.consultores.optiplant.aptiplantback.dto.response;

import java.math.BigDecimal;

public record DashboardResponse(
    BigDecimal ventasDelDia,
    BigDecimal ventasDelMes,
    Long alertasActivas,
    Long transferenciasPendientes,
    Long ordenesCompraPendientes
) {
}
