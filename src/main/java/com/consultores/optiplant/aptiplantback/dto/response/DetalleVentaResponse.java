package com.consultores.optiplant.aptiplantback.dto.response;

import java.math.BigDecimal;

public record DetalleVentaResponse(
    Long id,
    Long productoId,
    String productoNombre,
    BigDecimal cantidad,
    BigDecimal precioUnitario,
    BigDecimal descuentoLinea,
    BigDecimal subtotal
) {
}
