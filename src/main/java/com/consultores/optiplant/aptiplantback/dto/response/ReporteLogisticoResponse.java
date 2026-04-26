package com.consultores.optiplant.aptiplantback.dto.response;

import java.math.BigDecimal;

public record ReporteLogisticoResponse(
    Long transferenciaId,
    String estado,
    BigDecimal porcentajeCumplimiento,
    BigDecimal faltanteTotal
) {
}

