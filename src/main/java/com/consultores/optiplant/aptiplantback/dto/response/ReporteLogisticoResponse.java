package com.consultores.optiplant.aptiplantback.dto.response;

import java.math.BigDecimal;

/**
 * DTO para la respuesta de un reporte de logística.
 */
public record ReporteLogisticoResponse(
    Long transferenciaId,
    String estado,
    BigDecimal porcentajeCumplimiento,
    BigDecimal faltanteTotal
) {
}

