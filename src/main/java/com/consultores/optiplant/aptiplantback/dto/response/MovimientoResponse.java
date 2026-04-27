package com.consultores.optiplant.aptiplantback.dto.response;

import com.consultores.optiplant.aptiplantback.enums.TipoMovimiento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoResponse(
    Long id,
    Long inventarioId,
    TipoMovimiento tipo,
    BigDecimal cantidad,
    String motivo,
    String referenciaDocumento,
    LocalDateTime fecha,
    BigDecimal stockAntes,
    BigDecimal stockDespues,
    Long usuarioId
) {
}
