package com.consultores.optiplant.aptiplantback.dto.response;

import java.math.BigDecimal;

/**
 * DTO para la respuesta de un detalle de una venta.
 */
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
