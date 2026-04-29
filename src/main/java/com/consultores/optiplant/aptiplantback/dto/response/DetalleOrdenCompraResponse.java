package com.consultores.optiplant.aptiplantback.dto.response;

import java.math.BigDecimal;

/**
 * DTO para la respuesta de un detalle de una orden de compra.
 */
public record DetalleOrdenCompraResponse(
    Long id,
    Long productoId,
    String productoNombre,
    String productoSku,
    BigDecimal cantidadPedida,
    BigDecimal cantidadRecibida,
    BigDecimal precioUnitario,
    BigDecimal descuento,
    BigDecimal subtotal
) {
}
