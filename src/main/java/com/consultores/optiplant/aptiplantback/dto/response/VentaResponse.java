package com.consultores.optiplant.aptiplantback.dto.response;

import com.consultores.optiplant.aptiplantback.enums.EstadoVenta;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VentaResponse(
    Long id,
    Long sucursalId,
    Long usuarioId,
    Long listaPreciosId,
    LocalDateTime fecha,
    BigDecimal subtotal,
    BigDecimal descuentoGlobal,
    BigDecimal total,
    EstadoVenta estado,
    String motivoAnulacion
) {
}

